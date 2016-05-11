package sz.heytomato.justcompass;

/*
* 需求：写一款可以用于定位方向的指南针
*
* 思路：通过手机的加速度传感器和地磁传感器来实现*/

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    //传入SensorManager类成员变量sensorManager
    private SensorManager sensorManager;

    //传入ImageView类成员变量compassImg
    private ImageView compassImg;


    //重写onCreate函数，覆写父类方法，设置主活动为activity_main，并隐藏默认标题栏
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        //将title布局中的title_more按钮实例化
        Button titleMore=(Button)findViewById(R.id.button_more);

        //获取inflater实例，实例化为View为popupView并通过inflate将布局popup_windows中的控件传入,创建popupwindow实例
        LayoutInflater inflater=LayoutInflater.from(this);
        View popupView =inflater.inflate(R.layout.popup_window,null);
        final PopupWindow pop = new PopupWindow(popupView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, false);


        //实例化set_button按钮，并设置点击事件为弹出toast窗口显示“敬请期待”，之后隐藏pop窗口
        Button setButton=(Button)popupView.findViewById(R.id.set_button);
        setButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(),"敬请期待", Toast.LENGTH_SHORT).show();
                pop.dismiss();
            }
        });


        //实例化about_button按钮，并设置点击事件为链接至InformationActivity活动，并主活动设置在栈顶，之后隐藏pop窗口
        Button aboutButton=(Button)popupView.findViewById(R.id.about_button);
        aboutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, InformationActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); //注意本行的FLAG设置
                startActivity(intent);
                pop.dismiss();
            }
        });



        //设置点击窗口外边窗口消失
        pop.setOutsideTouchable(true);

        // 设置此参数获得焦点，否则无法点击
        pop.setFocusable(true);
        titleMore.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(pop.isShowing()) {
                    // 隐藏窗口，如果设置了点击窗口外小时即不需要此方式隐藏
                    pop.dismiss();
                } else {
                    // 显示窗口
                    pop.showAsDropDown(v);

                }
            }
        });

        //实例化compass_img图片
        compassImg =(ImageView)findViewById(R.id.compass_img);

        //实例化sensorManager
        sensorManager=(SensorManager)getSystemService(Context.SENSOR_SERVICE);

        //获取地磁传感器和加速度传感器实例
        Sensor magneticSensor=sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        Sensor accelerometerSensor=sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        //为地磁传感器和加速度传感器注册监听器
        sensorManager.registerListener(listener,magneticSensor,SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(listener,accelerometerSensor,SensorManager.SENSOR_DELAY_GAME);
    }

    //重写onDestroy方法，覆写父类onDestroy方法，判断sensorManager传感器管理器不为空时，注销监听器listener
    @Override
    protected void onDestroy(){
        super.onDestroy();
        if (sensorManager!=null){
            sensorManager.unregisterListener(listener);
        }
    }



    private SensorEventListener listener =new SensorEventListener() {

        //创建float[]类型变量accelerometerValues和magneticValues，并初始化
        float[] accelerometerValues=new float[3];
        float[] magneticValues=new float[3];

        //创建float类型变量lastRotateDegree
        private float lastRotateDegree;

        //创建onSensorChange方法
        @Override
        public void onSensorChanged(SensorEvent event){
            //判断当前是加速度传感器还是地磁传感器
            if (event.sensor.getType()==Sensor.TYPE_ACCELEROMETER){
                //注意赋值时要调用clone()方法
                accelerometerValues=event.values.clone();
            }else if (event.sensor.getType()==Sensor.TYPE_MAGNETIC_FIELD){
                //注意赋值时要调用clone()方法
                magneticValues=event.values.clone();
            }

            //创建类型为float[]的变量R和values
            float[]R=new float[9];
            float[]values=new float[3];

            //调用getTotationMatrix为R数组赋值，取得数组accelerometerValues和magneticValues的值
            SensorManager.getRotationMatrix(R,null,accelerometerValues,magneticValues);

            //调用getOrientation为values赋值R数组
            SensorManager.getOrientation(R,values);

            //将计算出的旋转角度取反，用于旋转指南针背景图
            float rotateDegree=-(float)Math.toDegrees(values[0]);
            if (Math.abs(rotateDegree-lastRotateDegree)>1){
                RotateAnimation animation=new RotateAnimation(lastRotateDegree,rotateDegree, Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
                animation.setFillAfter(true);
                compassImg.startAnimation(animation);
                lastRotateDegree=rotateDegree;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {
        }
    };
}

