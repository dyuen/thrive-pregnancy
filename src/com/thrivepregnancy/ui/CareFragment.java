package com.thrivepregnancy.ui;

import com.thrivepregnancy.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Implements the "My Care" fragment in the {@link MainActivity} page
 */
public class CareFragment extends Fragment {

	/**
	 * Empty public constructor required per the {@link Fragment} API documentation
	 */
	public CareFragment(){}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	    // Inflate the layout for this fragment
		View fragmentView = inflater.inflate(R.layout.fragment_care, container, false);
		return fragmentView;
	}
}
