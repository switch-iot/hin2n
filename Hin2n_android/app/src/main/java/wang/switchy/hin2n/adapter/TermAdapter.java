package wang.switchy.hin2n.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import wang.switchy.hin2n.R;

public class TermAdapter extends BaseQuickAdapter<String, BaseViewHolder> {


    public TermAdapter(List<String> list) {
        super(R.layout.term_line_tv, list);
    }


    @Override
    protected void convert(@NotNull BaseViewHolder helper, @NotNull String item) {
        helper.setText(R.id.term_text,item);
    }
}