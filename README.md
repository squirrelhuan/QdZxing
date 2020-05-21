# QdZxing
二维码扫描与生成(基于Zxing)

方便开发者以view的形式集成到页面中，方便自定义页面布局。


layout
```java
<RelativeLayout
        android:layout_width="200dp"
        android:layout_height="200dp"
        android:layout_marginTop="50dp"
        android:layout_gravity="center">
        <!--背景层用于显示camera视图-->
        <cn.demomaster.qdzxinglibrary.ScanSurfaceView
            android:id="@+id/ssfv"
            android:layout_width="match_parent"
            android:layout_height="match_parent"/>
        <!--遮罩层用于显示扫描窗口和扫描动画-->
        <cn.demomaster.qdzxinglibrary.ScanMakerView
            android:id="@+id/smv"
            android:layout_width="match_parent"
            android:layout_height="match_parent"/>
    </RelativeLayout>
```
