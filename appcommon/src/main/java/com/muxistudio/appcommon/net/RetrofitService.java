package com.muxistudio.appcommon.net;

import com.muxistudio.appcommon.data.ApartmentData;
import com.muxistudio.appcommon.data.AttentionBook;
import com.muxistudio.appcommon.data.AuditCourse;
import com.muxistudio.appcommon.data.BannerData;
import com.muxistudio.appcommon.data.Book;
import com.muxistudio.appcommon.data.BookId;
import com.muxistudio.appcommon.data.BookPost;
import com.muxistudio.appcommon.data.BookSearchResult;
import com.muxistudio.appcommon.data.BorrowedBook;
import com.muxistudio.appcommon.data.CalendarData;
import com.muxistudio.appcommon.data.CardData;
import com.muxistudio.appcommon.data.ClassRoom;
import com.muxistudio.appcommon.data.Course;
import com.muxistudio.appcommon.data.CourseId;
import com.muxistudio.appcommon.data.EleRequestData;
import com.muxistudio.appcommon.data.Electricity;
import com.muxistudio.appcommon.data.Hint;
import com.muxistudio.appcommon.data.Msg;
import com.muxistudio.appcommon.data.News;
import com.muxistudio.appcommon.data.PatchData;
import com.muxistudio.appcommon.data.ProductData;
import com.muxistudio.appcommon.data.RenewData;
import com.muxistudio.appcommon.data.Score;
import com.muxistudio.appcommon.data.SplashData;
import com.muxistudio.appcommon.data.UserInfo;
import com.muxistudio.appcommon.data.VerifyResponse;
import com.muxistudio.appcommon.data.VersionData;
import com.muxistudio.appcommon.data.WebsiteData;

import java.util.HashMap;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import rx.Observable;


/**
 * Created by ybao on 16/4/28.
 */
public interface RetrofitService {

    @GET("info/login/")
    Call<ResponseBody> mainLogin(@Header("Authorizat") String verification);

    @GET("lib/login/")
    Observable<Response<VerifyResponse>> libLogin(@Header("Authorization") String verification);

    @GET("lib/search/")
    Observable<BookSearchResult> searchBook(@Query("keyword") String keyword,
                                            @Query("page") int page);

    @GET("lib/detail/{id}/")
    Observable<Book> getBookDetail(@Path("id") String id);

    @GET("lib/me/")
    Observable<List<BorrowedBook>> getPersonalBook(@Header("s") String verification);

    /**
     * 200 OK
     * 403 禁止访问
     * 404 无关注图书
     * 502 服务器端错误
     */
    @GET("lib/attention/")
    Observable<Response<List<AttentionBook>>> getAttentionBooks(
            @Header("sid") String verification);

    /**
     * 200 添加关注成功
     * 401 已关注
     */
    @POST("lib/create/")
    Observable<Response<VerifyResponse>> createAttentionBook(
            @Header("sid") String verification, @Body
            BookPost bookPost);

    /**
     * 200 OK
     * 404 未找到图书
     */
    @HTTP(method = "DELETE", path = "lib/delete/", hasBody = true)
    Observable<Response<VerifyResponse>> delAttentionBook(
            @Header("sid") String verification,
            @Body BookId id);

    /**
     * 200
     * 406 不到续借时间
     * 403 超过最大续借次数
     * 400 请求无效
     */
    @POST("lib/renew/")
    Observable<Response<VerifyResponse>> renewBook(@Header("s") String verification, @Header("captcha") String captcha,
                                                   @Body RenewData renewData);

    //获取用户课表
    @GET("table/")
    Observable<List<Course>> getTimeTable();


    //添加课程
    @POST("table/")
    Observable<CourseId> addCourse(@Body Course course);

    //删除课程
    @DELETE("table/{id}/")
    Observable<Response<VerifyResponse>> deleteCourse(@Path("id") String id);

    @PUT("table/{id}/")
    Observable<Response<VerifyResponse>> updateCourse(@Path("id") String id,
                                                      @Body Course course);

    @GET("webview_info/")
    Observable<List<News>> getNews();

    @GET("msg/")
    Observable<Hint> getHint();


    @POST("cache/userinfo/")
    Observable<Msg> postUserInfo(@Body UserInfo user);

    @GET("calendar/")
    Observable<CalendarData> getCalendar();

    @GET("ios/banner/")
    Observable<List<BannerData>> getBanner();

    @GET("apartment/")
    Observable<List<ApartmentData>> getApartment();

    @POST("ele/")
    Observable<Response<Electricity>> getElectricity(@Body EleRequestData requestData);

    //蹭课 搜索蹭课结果:
    @GET("lesson/")
    Observable<AuditCourse> getAuditCourse(@QueryMap HashMap<String, String> map);

    //查询余额  除了学号其他传固定值 http://console.ccnu.edu
    // .cn/ecard/getTrans?userId=2013211389&days=90&startNum=0&num=200
    @GET("http://console.ccnu.edu.cn/ecard/getTrans")
    Observable<List<CardData>> getCardBalance(
            @Query("userId") String sid,
            @Query("days") String day,
            @Query("startNum") String start,
            @Query("num") String num);

    @GET("app/latest/")
    Observable<VersionData> getLatestVersion();

    @GET("patch/")
    Observable<List<PatchData>> getPatch();

    @GET("start/")
    Observable<SplashData> getSplash();

    @GET("product/")
    Observable<ProductData> getProduct();

    @GET("site/")
    Observable<List<WebsiteData>> getWebsite();

    @GET("classroom/get_classroom/")
    Observable<ClassRoom> getClassRoom(@Query("weekno") String week,
                                       @Query("weekday") String day,
                                       @Query("building") String area);

    //todo fix this!
    @GET("grade/")
    Observable<List<Score>> getScores(@Query("xnm") String year,
                                      @Query("xqm") String term);


}