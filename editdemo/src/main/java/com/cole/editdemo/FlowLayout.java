package com.cole.editdemo;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jimmy on 16/6/3.
 * 1.自定义ViewGroup容器,实现自动换行的流式布局的效果
 *   核心需要复写onMeasure和onLayout方法
 * 先写大小测算,在写位置的摆放
 * 扩展
 * ViewGroup难度点比较高
 */
public class FlowLayout extends ViewGroup {
    private List<Integer> lineMaxHeightList; //{40,80,33}
    //6个child
    //{{view1,view2},{view3},{view4,view5,view5}}
    //List<View>每一行的View的情况
    private List<List<View>> mAllViewList;

    public FlowLayout(Context context) {
        super(context);
        init();
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init(){
        lineMaxHeightList = new ArrayList<Integer>();
        mAllViewList = new ArrayList<List<View>>();
    }

    /**
     * 测算容器宽度和高度 (这里针对于自适应的情况需要处理)
     * onMeasure方法系统会多次调用,最后一次测算的值是最终的值
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.i("123","onMeasure");
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);  //包含测试的大小和模式
        //由于方法调用的此时不叫多,只需要记录最后一次有意义的数据,没有意义的数据都不保留
        lineMaxHeightList.clear();
        mAllViewList.clear();
        //解析widthMeasureSpec和heightMeasureSpec(size的记录,mode的记录)
        // match_parent (如果在外层的,系统给你屏幕的跨度)
        // wrap_content (child的宽度的累加还要加上margin(left,right))
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        //UNSPECIFIED模式:parent容器不做任何限制
        //AT_MOST模式:child可以尽可能的大,但不能超过parent
        //EXACTLY模式:parent给定义的大小
        int widthMode = MeasureSpec.getMode(widthMeasureSpec); //三种模式
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        //处理自适应的情况
        int wrapWidth = 0, wrapHeight = 0;
        //定义每一行的宽度,高度
        int lineWidth = 0, lineHeight=0;

        List<View> mList = new ArrayList<View>();
        //由child决定
        int cCount = getChildCount();
        for (int i = 0; i <cCount ; i++) {
            View child = getChildAt(i);
            //child填写自适应怎么处理
            //容器去测量child是自适应的情况
            measureChild(child,widthMeasureSpec,heightMeasureSpec); //可以测试child
            //获取该child实际的宽度和高度
            //补上外边距
            MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams(); //动态代码获取外边距的代码
            int cWidth = child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
            int cHeight = child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
            //实现自动换行的效果 (什么时候需要换行,并记录自适应的宽度)
            if(lineWidth + cWidth <= widthSize){ //没有超过容器的最大宽度
                lineWidth += cWidth;
                //去最大的高度
                lineHeight = Math.max(lineHeight,cHeight); //保持最大值
                //每次加入当前child
                mList.add(child);
            }else {
                //加入该行的作答高度
                lineMaxHeightList.add(lineHeight); //记录上一行的最大高度
                //换行的时候加入到容器里,上一行所有的child
                mAllViewList.add(mList);
                //不可以清除集合,要创建新的对象
                mList = new ArrayList<View>(); //新的空间地址
                //上一行的lineWidth,考虑每一行宽度的情况,最终自适应的宽度应该是每一行的最大值
                wrapWidth = Math.max(wrapWidth,lineWidth);  //最大行的值
                wrapHeight += lineHeight; //高度不断累加(上一行自适应的高度)
                //行的宽度和高度设置成当前的child宽度和高度
                lineWidth = cWidth;
                lineHeight = cHeight;
                mList.add(child); //换行的第一个控件
            }
            //最后一个child情况
            if(i == cCount -1){
                //加入该行的作答高度
                lineMaxHeightList.add(lineHeight); //记录该行的最大高度
                mAllViewList.add(mList); //补上当前行的集合,放入到全局的
                //加入最后一个child的时候补上当前行的自适应的宽度和高度
                wrapWidth = Math.max(wrapWidth,lineWidth);  //最大行的值
                wrapHeight += lineHeight; //高度不断累加(上一行自适应的高度)
            }
            //自适应的宽度和高度计算就结束
            //包含mode,size情况 (通过模式判断知道什么时候使用自己自适应的值)
            Log.i("123","wrapWidth-->"+ wrapWidth);
            Log.i("123","wrapHeight-->"+ wrapHeight);
            int lastWidthSizeMode = widthMode == MeasureSpec.EXACTLY ? widthSize : wrapWidth;
            int lastHeightSizeMode = heightMode == MeasureSpec.EXACTLY ? heightSize :wrapHeight;
            //重新测试 (符合自己设定的规则)
            setMeasuredDimension(lastWidthSizeMode,lastHeightSizeMode);
        }
    }

    /**
     * 让容器支持MarginLayoutParams的使用
     */
    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        //让MarginLayoutParams可以使用 (第一个参数:上下文 ,第二个参数:AttributeSet属性)
        return new MarginLayoutParams(getContext(),attrs);
    }

    /**
     * 确定容器中child的位置摆放
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        //onMeasure可以记录 ,onLayout就可以使用了
        //每一行的View必须要保存下来
        //每一行的最大高度记录下来 (全局变量定义容器)
        //这样的话就可以放置正确的位置了
        int lineCount = mAllViewList.size(); //全局集合的size都可以
        //定义坐上叫的坐标
        int left =0, top =0, right = 0, bottom = 0;
        Log.i("123","view size-->" + lineCount);
        for (int i = 0; i < lineCount; i++) {
            List<View> views = mAllViewList.get(i);
            //每一个单行的空间应该如何放置正确坐标
            for (int j = 0; j < views.size(); j++) {
                View child = views.get(j);
                MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
                //child的左外边距 (下一次要补上的)
                /**
                 * 举例 : left,10 right,10  child 100
                 */
                if(i == 0){
                    top = lp.topMargin;
                }
                left += lp.leftMargin;    //10      //2次 130  250
                //右下角
                right =  left + child.getMeasuredWidth();  //230
                bottom = top + child.getMeasuredHeight();
                //child放置位置的方法
                child.layout(left,top,right,bottom);  //每个child根据自己计算的上下左右的坐标,放置正确的位置
                //改变left  (130 + 100+ 10)  240 (不需要多加left)
                left += child.getMeasuredWidth() + lp.rightMargin;
                //顶部的改变换行就有效果
            }
            //换行
            top += lineMaxHeightList.get(i); //补上最大高度
            Log.i("123","每行高度的变化"+top);
            left = 0 ; //还原回0
        }
    }
}