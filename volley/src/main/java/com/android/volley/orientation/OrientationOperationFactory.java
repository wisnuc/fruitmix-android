package com.android.volley.orientation;

/**
 * Created by Administrator on 2016/10/21.
 */

public class OrientationOperationFactory {

    public static OrientationOperation createOrientationOperation(int number) {

        OrientationOperation orientationOperation = null;

        switch (number) {
            case 1:
                orientationOperation = new Orientation1Operation();
                break;
            case 2:
                orientationOperation = new Orientation2Operation();
                break;
            case 3:
                orientationOperation = new Orientation3Operation();
                break;
            case 4:
                orientationOperation = new Orientation4Operation();
                break;
            case 5:
                orientationOperation = new Orientation5Operation();
                break;
            case 6:
                orientationOperation = new Orientation6Operation();
                break;
            case 7:
                orientationOperation = new Orientation7Operation();
                break;
            case 8:
                orientationOperation = new Orientation8Operation();
                break;

        }

        return orientationOperation;
    }

}
