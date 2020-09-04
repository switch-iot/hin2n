/**
 * (C) 2007-18 - ntop.org and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not see see <http://www.gnu.org/licenses/>
 *
 */

#include "n2n.h"

#include <edge_jni/edge_jni.h>
#include <tun2tap/tun2tap.h>
#include <n2n.h>

#define N2N_NETMASK_STR_SIZE    16 /* dotted decimal 12 numbers + 3 dots */
#define N2N_MACNAMSIZ           18 /* AA:BB:CC:DD:EE:FF + NULL*/
#define N2N_IF_MODE_SIZE        16 /* static | dhcp */
#define ARP_PERIOD_INTERVAL     10 /* sec */

/* Shared status. Must call pthread_mutex_lock before use. */
n2n_edge_status_t* g_status;

static n2n_mac_t broadcast_mac = { 0xff, 0xff, 0xff, 0xff, 0xff, 0xff };
static n2n_mac_t null_mac = {0, 0, 0, 0, 0, 0};

/* ***************************************************** */

/* Private status. Can be accessed without lock. */
typedef struct {
  uint32_t gateway_ip;
  n2n_mac_t gateway_mac;
  n2n_edge_conf_t *conf;
  uint8_t tap_mac[6];
  uint32_t tap_ipaddr;
  time_t lastArpPeriod;
} n2n_android_t;

/* ***************************************************** */

static char arp_packet[] = {
			    0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, /* Dest mac */
			    0x00, 0x00, 0x00, 0x00, 0x00, 0x00, /* Src mac */
			    0x08, 0x06, /* ARP */
			    0x00, 0x01, /* Ethernet */
			    0x08, 0x00, /* IP */
			    0x06, /* Hw Size */
			    0x04, /* Protocol Size */
			    0x00, 0x01, /* ARP Request */
			    0x00, 0x00, 0x00, 0x00, 0x00, 0x00, /* Src mac */
			    0x00, 0x00, 0x00, 0x00, /* Src IP */
			    0x00, 0x00, 0x00, 0x00, 0x00, 0x00, /* Target mac */
			    0x00, 0x00, 0x00, 0x00 /* Target IP */
};

/* ************************************** */

static int build_unicast_arp(char *buffer, size_t buffer_len,
	  uint32_t target, n2n_android_t *priv) {
  if(buffer_len < sizeof(arp_packet)) return(-1);

  memcpy(buffer, arp_packet, sizeof(arp_packet));
  memcpy(&buffer[6], priv->tap_mac, 6);
  memcpy(&buffer[22], priv->tap_mac, 6);
  memcpy(&buffer[28], &priv->tap_ipaddr, 4);
  memcpy(&buffer[32], broadcast_mac, 6);
  memcpy(&buffer[38], &target, 4);
  return(sizeof(arp_packet));
}

/* ***************************************************** */

/** Find the address and IP mode for the tuntap device.
 *
 *  s is one of these forms:
 *
 *  <host> := <hostname> | A.B.C.D
 *
 *  <host> | static:<host> | dhcp:<host>
 *
 *  If the mode is present (colon required) then fill ip_mode with that value
 *  otherwise do not change ip_mode. Fill ip_mode with everything after the
 *  colon if it is present; or s if colon is not present.
 *
 *  ip_add and ip_mode are NULL terminated if modified.
 *
 *  return 0 on success and -1 on error
 */
static int scan_address(char * ip_addr, size_t addr_size,
                        char * ip_mode, size_t mode_size,
                        const char * s) {
  int retval = -1;
  char * p;

  if((NULL == s) || (NULL == ip_addr))
    {
      return -1;
    }

  memset(ip_addr, 0, addr_size);

  p = strpbrk(s, ":");

  if(p)
    {
      /* colon is present */
      if(ip_mode)
        {
	  size_t end=0;

	  memset(ip_mode, 0, mode_size);
	  end = MIN(p-s, (ssize_t)(mode_size-1)); /* ensure NULL term */
	  strncpy(ip_mode, s, end);
	  strncpy(ip_addr, p+1, addr_size-1); /* ensure NULL term */
	  retval = 0;
        }
    }
  else
    {
      /* colon is not present */
      strncpy(ip_addr, s, addr_size);
    }

  return retval;
}

/* *************************************************** */

static const char *random_device_mac(void)
{
  const char key[] = "0123456789abcdef";
  static char mac[18];
  int i;

  srand(getpid());
  for (i = 0; i < sizeof(mac) - 1; ++i) {
    if ((i + 1) % 3 == 0) {
      mac[i] = ':';
      continue;
    }
    mac[i] = key[random() % sizeof(key)];
  }
  mac[sizeof(mac) - 1] = '\0';
  return mac;
}

/* *************************************************** */

static int protect_socket(int sock) {
  JNIEnv *env = NULL;

  if(!sock)
    return(-1);

  if(!g_status)
    return(-1);

  if ((*g_status->jvm)->GetEnv(g_status->jvm, (void**)&env, JNI_VERSION_1_1) != JNI_OK || !env) {
    traceEvent(TRACE_ERROR, "GetEnv failed");
    return(-1);
  }

  jclass vpn_service_cls = (*env)->GetObjectClass(env, g_status->jobj_service);

  if(!vpn_service_cls) {
    traceEvent(TRACE_ERROR, "GetObjectClass(VpnService) failed");
    return(-1);
  }

  /* Call VpnService protect */
  jmethodID midProtect = (*env)->GetMethodID(env, vpn_service_cls, "protect", "(I)Z");
  if(!midProtect) {
    traceEvent(TRACE_ERROR, "Could not resolve VpnService::protect");
    return(-1);
  }

  jboolean isProtected = (*env)->CallBooleanMethod(env, g_status->jobj_service, midProtect, sock);

  if(!isProtected) {
    traceEvent(TRACE_ERROR, "VpnService::protect failed");
    return(-1);
  }

  return(0);
}

/* *************************************************** */

/** Called periodically to update the gateway MAC address. The ARP reply packet
    is handled in handle_PACKET . */
static void update_gateway_mac(n2n_edge_t *eee) {
  n2n_android_t *priv = (n2n_android_t*) edge_get_userdata(eee);

  if(priv->gateway_ip != 0) {
    size_t len;
    char buffer[48];

    len = build_unicast_arp(buffer, sizeof(buffer), priv->gateway_ip, priv);
    traceEvent(TRACE_DEBUG, "Updating gateway mac");
    edge_send_packet2net(eee, (uint8_t*)buffer, len);
  }
}

/* *************************************************** */

static void on_sn_registration_updated(n2n_edge_t *eee, time_t now, const n2n_sock_t *sn) {
  int change = 0;

  pthread_mutex_lock(&g_status->mutex);
  change = g_status->running_status == EDGE_STAT_CONNECTED ? 0 : 1;
  g_status->running_status = EDGE_STAT_CONNECTED;
  pthread_mutex_unlock(&g_status->mutex);

  if(change)
    g_status->report_edge_status();

  update_gateway_mac(eee);
}

/* *************************************************** */

static n2n_verdict on_packet_from_peer(n2n_edge_t *eee, const n2n_sock_t *peer,
	  uint8_t *payload, uint16_t *payload_size) {
  n2n_android_t *priv = (n2n_android_t*) edge_get_userdata(eee);

  if((*payload_size >= 36) &&
     (ntohs(*((uint16_t*)&payload[12])) == 0x0806) && /* ARP */
     (ntohs(*((uint16_t*)&payload[20])) == 0x0002) && /* REPLY */
     (!memcmp(&payload[28], &priv->gateway_ip, 4))) { /* From gateway */
    memcpy(priv->gateway_mac, &payload[22], 6);

    traceEvent(TRACE_INFO, "Gateway MAC: %02X:%02X:%02X:%02X:%02X:%02X",
	       priv->gateway_mac[0], priv->gateway_mac[1], priv->gateway_mac[2],
	       priv->gateway_mac[3], priv->gateway_mac[4], priv->gateway_mac[5]);
  }

  uip_buf = payload;
  uip_len = *payload_size;
  if (IPBUF->ethhdr.type == htons(UIP_ETHTYPE_ARP)) {
    uip_arp_arpin();
    if (uip_len > 0) {
      traceEvent(TRACE_DEBUG, "ARP reply packet prepare to send");
      edge_send_packet2net(eee, uip_buf, uip_len);
      return N2N_DROP;
    }
  }

  return(N2N_ACCEPT);
}

/* *************************************************** */

static n2n_verdict on_packet_from_tap(n2n_edge_t *eee, uint8_t *payload,
	    uint16_t *payload_size) {
  n2n_android_t *priv = (n2n_android_t*) edge_get_userdata(eee);

  /* Fill destination mac address first or generate arp request packet instead of
   * normal packet. */
  uip_buf = payload;
  uip_len = *payload_size;
  uip_arp_out();
  if (IPBUF->ethhdr.type == htons(UIP_ETHTYPE_ARP))
  {
    *payload_size = uip_len;
    traceEvent(TRACE_DEBUG, "ARP request packets are sent instead of packets");
  }

  /* A NULL MAC as destination means that the packet is directed to the
   * default gateway. */
  if((*payload_size > 6) && (!memcmp(payload, null_mac, 6))) {
    traceEvent(TRACE_DEBUG, "Detected packet for the gateway");

    /* Overwrite the destination MAC with the actual gateway mac address */
    memcpy(payload, priv->gateway_mac, 6);
  }

  return(N2N_ACCEPT);
}

void on_main_loop_period(n2n_edge_t *eee, time_t now) {
  n2n_android_t *priv = (n2n_android_t *) edge_get_userdata(eee);

  /* call arp timer periodically  */
  if ((now - priv->lastArpPeriod) > ARP_PERIOD_INTERVAL) {
    uip_arp_timer();
    priv->lastArpPeriod = now;
  }
}

/* *************************************************** */

int start_edge_v2(n2n_edge_status_t* status)
{
  int     keep_on_running = 0;
  char    tuntap_dev_name[N2N_IFNAMSIZ] = "tun0";
  char    ip_mode[N2N_IF_MODE_SIZE]="static";
  char    ip_addr[N2N_NETMASK_STR_SIZE] = "";
  char    netmask[N2N_NETMASK_STR_SIZE]="255.255.255.0";
  char    device_mac[N2N_MACNAMSIZ]="";
  char *  encrypt_key=NULL;
  struct in_addr gateway_ip = {0};
  struct in_addr tap_ip = {0};
  n2n_edge_conf_t conf;
  n2n_edge_t *eee = NULL;
  n2n_edge_callbacks_t callbacks;
  n2n_android_t private_status;
  int i;
  tuntap_dev dev;
  uint8_t hex_mac[6];
  int rv = 0;

  if (!status) {
    traceEvent( TRACE_ERROR, "Empty cmd struct" );
    return 1;
  }
  g_status = status;
  n2n_edge_cmd_t* cmd = &status->cmd;

  setTraceLevel(cmd->trace_vlevel);
  FILE* fp = fopen(cmd->logpath, "a");
  if (fp == NULL) {
    traceEvent(TRACE_ERROR, "failed to open log file.");
  } else {
    setTraceFile(fp);
  }

  if (cmd->vpn_fd < 0) {
    traceEvent(TRACE_ERROR, "VPN socket is invalid.");
    return 1;
  }

  pthread_mutex_lock(&g_status->mutex);
  g_status->running_status = EDGE_STAT_CONNECTING;
  pthread_mutex_unlock(&g_status->mutex);
  g_status->report_edge_status();

  memset(&dev, 0, sizeof(dev));
  edge_init_conf_defaults(&conf);

  /* Load the configuration */
  strncpy((char *)conf.community_name, cmd->community, N2N_COMMUNITY_SIZE-1);

  if(cmd->enc_key && cmd->enc_key[0]) {
    conf.transop_id = N2N_TRANSFORM_ID_TWOFISH;
    conf.encrypt_key = strdup(cmd->enc_key);
    traceEvent(TRACE_DEBUG, "encrypt_key = '%s'\n", encrypt_key);
  }

  if(cmd->encryption_mode[0]) {
    if(!strcmp(cmd->encryption_mode, "Twofish"))
      conf.transop_id = N2N_TRANSFORM_ID_TWOFISH;
    else if(!strcmp(cmd->encryption_mode, "AES-CBC"))
      conf.transop_id = N2N_TRANSFORM_ID_AESCBC;
    else if(!strcmp(cmd->encryption_mode, "Speck-CTR"))
      conf.transop_id = N2N_TRANSFORM_ID_SPECK;
    else if(!strcmp(cmd->encryption_mode, "ChaCha20"))
      conf.transop_id = N2N_TRANSFORM_ID_CHACHA20;
    else
      traceEvent(TRACE_WARNING, "unknown encryption mode:'%s'\n", cmd->encryption_mode);
  }

  scan_address(ip_addr, N2N_NETMASK_STR_SIZE,
	       ip_mode, N2N_IF_MODE_SIZE,
	       cmd->ip_addr);

  dev.fd = cmd->vpn_fd;

  conf.drop_multicast = cmd->drop_multicast == 0 ? 0 : 1;
  conf.allow_routing = cmd->allow_routing == 0 ? 0 : 1;
  conf.dyn_ip_mode = (strcmp("dhcp", ip_mode) == 0) ? 1 : 0;

  for (i = 0; i < N2N_EDGE_NUM_SUPERNODES && i < EDGE_CMD_SUPERNODES_NUM; ++i)
    {
      if (cmd->supernodes[i][0] != '\0')
        {
	  strncpy(conf.sn_ip_array[conf.sn_num], cmd->supernodes[i], N2N_EDGE_SN_HOST_SIZE);
	  traceEvent(TRACE_DEBUG, "Adding supernode[%u] = %s\n", (unsigned int)conf.sn_num, (conf.sn_ip_array[conf.sn_num]));
	  ++conf.sn_num;
        }
    }

  if (cmd->ip_netmask[0] != '\0')
    strncpy(netmask, cmd->ip_netmask, N2N_NETMASK_STR_SIZE);

  if (cmd->gateway_ip[0] != '\0')
    inet_aton(cmd->gateway_ip, &gateway_ip);

  if (cmd->mac_addr[0] != '\0')
    strncpy(device_mac, cmd->mac_addr, N2N_MACNAMSIZ);
  else {
    strncpy(device_mac, random_device_mac(), N2N_MACNAMSIZ);
    traceEvent(TRACE_DEBUG, "random device mac: %s\n", device_mac);
  }

  str2mac(hex_mac, device_mac);

  if(edge_verify_conf(&conf) != 0) {
    if(conf.encrypt_key) free(conf.encrypt_key);
    conf.encrypt_key = NULL;
    traceEvent(TRACE_ERROR, "Bad configuration");
    rv = 1;
    goto cleanup;
  }

  /* Open the TAP device */
  if(tuntap_open(&dev, tuntap_dev_name, ip_mode, ip_addr, netmask, device_mac, cmd->mtu) < 0) {
    traceEvent(TRACE_ERROR, "Failed in tuntap_open");
    rv = 1;
    goto cleanup;
  }

  /* Start n2n */
  eee = edge_init(&dev, &conf, &i);

  if(eee == NULL) {
    traceEvent( TRACE_ERROR, "Failed in edge_init" );
    rv = 1;
    goto cleanup;
  }

  /* Protect the socket so that the supernode traffic won't go inside the n2n VPN */
  if(protect_socket(edge_get_n2n_socket(eee)) < 0) {
    traceEvent( TRACE_ERROR, "protect(n2n_socket) failed" );
    rv = 1;
    goto cleanup;
  }

  if(protect_socket(edge_get_management_socket(eee)) < 0) {
    traceEvent( TRACE_ERROR, "protect(management_socket) failed" );
    rv = 1;
    goto cleanup;
  }

  /* Private Status */
  memset(&private_status, 0, sizeof(private_status));
  private_status.gateway_ip = gateway_ip.s_addr;
  private_status.conf = &conf;
  memcpy(private_status.tap_mac, hex_mac, 6);
  inet_aton(ip_addr, &tap_ip);
  private_status.tap_ipaddr = tap_ip.s_addr;
  edge_set_userdata(eee, &private_status);

  /* set host addr, netmask, mac addr for UIP and init arp*/
  {
    int match, i;
    int ip[4];
    uip_ipaddr_t ipaddr;
    struct uip_eth_addr eaddr;

    match = sscanf(ip_addr, "%d.%d.%d.%d", ip, ip + 1, ip + 2, ip + 3);
    if (match != 4) {
      traceEvent(TRACE_ERROR, "scan ip failed, ip: %s", ip_addr);
      rv = 1;
      goto cleanup;
    }
    uip_ipaddr(ipaddr, ip[0], ip[1], ip[2], ip[3]);
    uip_sethostaddr(ipaddr);
    match = sscanf(netmask, "%d.%d.%d.%d", ip, ip + 1, ip + 2, ip + 3);
    if (match != 4) {
      traceEvent(TRACE_ERROR, "scan netmask error, ip: %s", netmask);
      rv = 1;
      goto cleanup;
    }
    uip_ipaddr(ipaddr, ip[0], ip[1], ip[2], ip[3]);
    uip_setnetmask(ipaddr);
    for (i = 0; i < 6; ++i)
      eaddr.addr[i] = hex_mac[i];
    uip_setethaddr(eaddr);

    uip_arp_init();
  }

  /* Set up the callbacks */
  memset(&callbacks, 0, sizeof(callbacks));
  callbacks.sn_registration_updated = on_sn_registration_updated;
  callbacks.packet_from_peer = on_packet_from_peer;
  callbacks.packet_from_tap = on_packet_from_tap;
  callbacks.main_loop_period = on_main_loop_period;
  edge_set_callbacks(eee, &callbacks);

  keep_on_running = 1;
  pthread_mutex_lock(&g_status->mutex);
  g_status->running_status = EDGE_STAT_CONNECTED;
  pthread_mutex_unlock(&g_status->mutex);
  g_status->report_edge_status();
  traceEvent(TRACE_NORMAL, "edge started");

  run_edge_loop(eee, &keep_on_running);

  traceEvent(TRACE_NORMAL, "edge stopped");

cleanup:
  if(eee) edge_term(eee);
  if(encrypt_key) free(encrypt_key);
  tuntap_close(&dev);
  edge_term_conf(&conf);

  return rv;
}

/* *************************************************** */

int stop_edge_v2(void)
{
  // quick stop
  int fd = open_socket(0, 0 /* bind LOOPBACK*/ );
  if (fd < 0) {
    return 1;
  }

  struct sockaddr_in peer_addr;
  peer_addr.sin_family = PF_INET;
  peer_addr.sin_addr.s_addr = htonl(INADDR_LOOPBACK);
  peer_addr.sin_port = htons(N2N_EDGE_MGMT_PORT);
  sendto(fd, "stop", 4, 0, (struct sockaddr *)&peer_addr, sizeof(struct sockaddr_in));
  close(fd);

  // Do not report the status yet, the edge thread may be still running
  /*
  pthread_mutex_lock(&g_status->mutex);
  g_status->running_status = EDGE_STAT_DISCONNECT;
  pthread_mutex_unlock(&g_status->mutex);
  g_status->report_edge_status();
   */

  return 0;
}
