package com.example.beak.fuckxiaomi;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by Beak on 2015/9/18.
 */
public class ResizeFragment extends Fragment {

    private EditText mWidEt, mHeiEt;
    private Button mGoBtn;

    private OnSizeNeedChangeListener mListener = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_resize, null, true);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mWidEt = (EditText)view.findViewById(R.id.resize_width);
        mHeiEt = (EditText)view.findViewById(R.id.resize_height);

        mGoBtn = (Button)view.findViewById(R.id.resize_go);
        mGoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    int width = Integer.parseInt(mWidEt.getText().toString());
                    int height = Integer.parseInt(mHeiEt.getText().toString());
                    if (mListener != null) {
                        mListener.onNeedChange(width, height);
                    }
                } catch (Exception e) {
                    Toast.makeText(getActivity(), "plz check width or height", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void setOnSizeNeedChangeListener (OnSizeNeedChangeListener listener) {
        mListener = listener;
    }

    public interface OnSizeNeedChangeListener {
        public void onNeedChange (int width, int height);
    }
}
