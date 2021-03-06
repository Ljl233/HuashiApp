package net.muxi.huashiapp.ui.score.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.muxistudio.appcommon.appbase.BaseAppFragment;
import com.muxistudio.appcommon.data.Score;
import com.muxistudio.appcommon.net.CampusFactory;
import com.muxistudio.appcommon.utils.UserUtil;
import com.muxistudio.common.util.ToastUtil;

import net.muxi.huashiapp.R;
import net.muxi.huashiapp.login.CcnuCrawler3;
import net.muxi.huashiapp.login.SingleCCNUClient;
import net.muxi.huashiapp.ui.score.adapter.CreditAdapter;
import net.muxi.huashiapp.utils.ScoreCreditUtils;

import org.json.JSONException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.HttpException;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static rx.plugins.RxJavaHooks.onError;

//当前已修学分
public class CurCreditFragment extends BaseAppFragment {

    private final static String TAG = "getScoresAndCredits";
    private static int ifReLogin = 0;

    private int startYear,endYear;
    private Date date=new Date();
    private List<Score> mCredit = new ArrayList<>();

    private TextView mTvTotalCredit;
    private ExpandableListView mElvCredit;

    private ViewPagerSlideListener mViewPagerSlideListener;

    private int getCurYear(){
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat format = new SimpleDateFormat("yyyy");
        Date date = new Date();

        int thisYear =  Integer.parseInt(format.format(date));
        //如果当前是2016 年 则自动选择 2016 - 2017 学年度的 学分
        if(startYear == (thisYear))
            thisYear = startYear + 1;

        return thisYear;
    }
    public static CurCreditFragment newInstance(int type){
        return new CurCreditFragment();
    }

    public List<Score> getCredit(){
        return mCredit;
    }

    public Observable<List<Score>>[] getScoreRequest(int start, int end) {
        Observable<List<Score>>[] observables = new Observable[(end - start)];
        for (int i = 0; i < (end - start); i++) {
            observables[i] = CampusFactory.getRetrofitService()
                    .getScores(String.valueOf(start + i), "");
            observables[i]=  SingleCCNUClient.getClient().getScores(String.valueOf(start + i), "",false, String.valueOf(date.getTime()),100,1,"","asc",1 )
                                .flatMap(new Func1<ResponseBody, Observable<List<Score>>>() {
                                    @Override
                                    public Observable<List<Score>> call(ResponseBody responseBody) {
                                        List<Score>list=null;
                                        try {
                                            list=ScoreCreditUtils.getScoreFromJson(responseBody.string());
                                        } catch (JSONException e) {
                                            if ( e.getMessage().equals("cookie expired") && ifReLogin == 0 ) {
                                                //如果cookie过期 获取成绩失败，要重新登陆
                                                ifReLogin = 1;
                                                performLogin();
                                                onError(e);
                                            } else {
                                                e.printStackTrace();
                                                hideLoading();
                                                ToastUtil.showShort(R.string.score_error_1);
                                            }
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                            hideLoading();
                                            ToastUtil.showShort(R.string.score_error_1);
                                        }
                                        return Observable.just(list);
                                    }
                                });
        }
        return observables;
    }

    //cookie如果失效 需要重新登陆
    public void performLogin () {
        CcnuCrawler3 ccnuCrawler3 = new CcnuCrawler3();
        ccnuCrawler3.performLoginSystem(new Subscriber<ResponseBody>() {
            @Override
            public void onCompleted() {
                Log.i(TAG, "onCompleted: ");
                ccnuCrawler3.getClient().saveCookieToLocal();
                loadCredit();
            }

            @Override
            public void onError(Throwable e) {
                if (e instanceof HttpException) {
                    Log.e(TAG, "onError: httpexception code " + ((HttpException) e).response().code());
                    try {
                        Log.e(TAG, "onError:  httpexception errorbody: " + ((HttpException) e).response().errorBody().string());
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                } else if (e instanceof NullPointerException)
                    Log.e(TAG, "onError: null   " + e.getMessage());
                else
                    Log.e(TAG, "onError: ");
                e.printStackTrace();
            }

            @Override
            public void onNext(ResponseBody responseBody) {
                Log.i(TAG, "onNext: " + "login success");
            }
        });

    }

    public void loadCredit(Observable<List<Score>>[] listObservable) {
        showLoading();
        Observable<List<Score>> scoreObservable =
                Observable.merge(listObservable, 5)
                .flatMap((Func1<List<Score>, Observable<Score>>) Observable::from)
                .toList();

        scoreObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(scores -> {
                    hideLoading();

                    mCredit = scores;

                    HashMap<String,Double> creditValueMap = ScoreCreditUtils.getSortedGroupCreditMap(scores);
                    HashMap<String,List<Score>> scoreMap  = ScoreCreditUtils.getSortedCourseTypeMap(scores);

                    List<List<Score>> sortedList = ScoreCreditUtils.getTypeOrderedList(scoreMap);
                    List<String> classType = ScoreCreditUtils.getCreditTypes();
                    List<Double> courseCredits = ScoreCreditUtils.getCreditValues(creditValueMap);

                    mElvCredit.setGroupIndicator(null);
                    CreditAdapter adapter = new CreditAdapter(getActivity(),sortedList,classType,courseCredits);
                    mElvCredit.setAdapter(adapter);

                    double totalValue = ScoreCreditUtils.getCreditTotal(creditValueMap);
                    mTvTotalCredit.setText(String.valueOf(totalValue));
                }, Throwable::printStackTrace, () -> {
                });
    }

    /**
     * the wrapper of the credit loading {@link #loadCredit(Observable[])}
     */
    public void loadCredit(){
        loadCredit(getScoreRequest(startYear,endYear));
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startYear = Integer.parseInt(UserUtil.getStudentFirstYear());
        endYear = getCurYear();

        //loadCredit
        //todo to solve the 403 problem
    }

    public void setViewPagerListener(ViewPagerSlideListener listener){
        if(listener == null)
            this.mViewPagerSlideListener = listener;

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_credit,container,false);
        initView(view);
        return view;
    }

    private void initView(View view){
        mElvCredit = view.findViewById(R.id.eplv_credit);
        mTvTotalCredit = view.findViewById(R.id.tv_credit_total_value);
        ifReLogin = 0;
    }

    interface ViewPagerSlideListener{
        void onSlideRight();
    }
}
