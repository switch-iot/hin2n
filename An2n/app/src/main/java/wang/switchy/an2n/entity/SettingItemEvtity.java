package wang.switchy.an2n.entity;

/**
 * Created by janiszhang on 2018/5/5.
 */

public class SettingItemEvtity {
    private String mSettingName;
    private Long mSaveId;
    private boolean isSelected;



    public SettingItemEvtity(String settingName, Long saveId, boolean isSelected) {
        mSettingName = settingName;
        mSaveId = saveId;
        this.isSelected = isSelected;
    }

    public String getSettingName() {
        return mSettingName;
    }

    public void setSettingName(String settingName) {
        mSettingName = settingName;
    }

    public Long getSaveId() {
        return mSaveId;
    }

    public void setSaveId(Long saveId) {
        mSaveId = saveId;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public OnMoreBtnClickListener mOnMoreBtnClickListener;

    public void setOnMoreBtnClickListener(OnMoreBtnClickListener onMoreBtnClickListener) {
        mOnMoreBtnClickListener = onMoreBtnClickListener;
    }

    public interface OnMoreBtnClickListener {
        void onClick();
    }
}
