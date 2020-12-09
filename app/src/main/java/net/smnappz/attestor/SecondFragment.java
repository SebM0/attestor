package net.smnappz.attestor;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import net.smnappz.attestor.qrcode.ErrorCorrectionLevel;
import net.smnappz.attestor.qrcode.Mode;
import net.smnappz.attestor.qrcode.QRCode;

import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

public class SecondFragment extends Fragment {
    private ImageView mImageView;
    private float mScaleFactor = 1.0f;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_second, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mImageView = view.findViewById(R.id.imageView);

//        view.findViewById(R.id.button_second).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                NavHostFragment.findNavController(SecondFragment.this)
//                        .navigate(R.id.action_SecondFragment_to_FirstFragment);
//            }
//        });
        final GestureDetector gd = new GestureDetector(requireActivity(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                if (mScaleFactor > 2.0f) {
                    // scroll horizontal & vertical
                    //TODO ensure we do not scroll outside image
                    int scrollX = mImageView.getScrollX();
                    int width = mImageView.getWidth();
                    mImageView.scrollBy((int)distanceX, (int)distanceY);
                    return true;
                } else {
                    // scroll vertical only
                    mImageView.scrollBy(0, (int)distanceY);
                }
                return false;
            }
            @Override
            public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float vX, float vY) {
                Log.println(Log.INFO, "Touch", String.format("%.2f - %.2f", vX, vY));
                if (Math.abs(vX) > 2*Math.abs(vY) && vX > 500) {
                    NavHostFragment.findNavController(SecondFragment.this)
                            .navigate(R.id.action_SecondFragment_to_FirstFragment);
                    return true;
                }
                return super.onFling(motionEvent, motionEvent1, vX, vY);
            }
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                Log.i("TAG", "onDoubleTap: " + e.toString());
                int scrollX = 0;
                int scrollY = 0;
                if (mScaleFactor < 1.9f || mScaleFactor > 2.1f) {
                    boolean lowerPane = mImageView.getScrollY() + (int)(e.getY()  / mScaleFactor) > mImageView.getHeight()/2;
                    scrollY = lowerPane ? mImageView.getHeight()/4 : - mImageView.getHeight()/4;
                    mScaleFactor = 2.0f;
                }
                else {
                    scrollX = mImageView.getScrollX() + (int)((e.getX() - mImageView.getWidth()/2) / mScaleFactor);
                    scrollY = mImageView.getScrollY() + (int)((e.getY() - mImageView.getHeight()/2) / mScaleFactor);
                    mScaleFactor = 4.0f;
                }
                // scale
                setScale(mScaleFactor);
                // scroll to zoom point
                mImageView.scrollTo(scrollX, scrollY);
                return true;
            }
        });
        final ScaleGestureDetector sd = new ScaleGestureDetector(requireActivity(), new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector scaleGestureDetector){
                setScale(mScaleFactor * scaleGestureDetector.getScaleFactor());
                return true;
            }
        });
        view.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                // Scale then other gestures
                boolean scaleHandle = sd.onTouchEvent(motionEvent);
                boolean gestureHandle = gd.onTouchEvent(motionEvent);
                return scaleHandle || gestureHandle;
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        if (getArguments() == null) {
            setArguments(((MainActivity)requireActivity()).getOptions());
        }
        String qr = getArguments().getString("QR");
        String date = getArguments().getString("date");
        String time = getArguments().getString("time");
        // create QRCode
        QRCode code = QRCode.getMinimumQRCode(qr, ErrorCorrectionLevel.M);
        Bitmap codeImage = code.createImage(10, 10);
        Bitmap att = BitmapFactory.decodeResource(getResources(), R.mipmap.attestation);
        Bitmap newBitmap =Bitmap.createBitmap(att.getWidth(),att.getHeight()*2,Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawBitmap(att, 0, 0,null);
        canvas.drawBitmap(Bitmap.createScaledBitmap(codeImage,(int)(codeImage.getWidth()*0.22), (int)(codeImage.getHeight()*0.22), true), 540, 870, null);
        int border = (att.getWidth() - codeImage.getWidth()) / 2;
        canvas.drawBitmap(codeImage, border, att.getHeight() + border, null);
        Paint paint = new Paint();
        paint.setColor(0);
        paint.setAlpha(255);
        paint.setTextSize(16);
        paint.setAntiAlias(true);
        canvas.drawText(date, 82, 963, paint);
        canvas.drawText(time, 285, 963, paint);
        // Display in image view
        mImageView.setImageBitmap(newBitmap);
        setScale(2.0f);
        mImageView.scrollTo(0, -att.getWidth()/2);
    }

    private void setScale(float scale) {
        mScaleFactor = scale;
        mImageView.setScaleX(mScaleFactor);
        mImageView.setScaleY(mScaleFactor);
    }
}