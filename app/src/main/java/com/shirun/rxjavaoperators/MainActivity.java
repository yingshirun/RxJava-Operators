package com.shirun.rxjavaoperators;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.jakewharton.rxbinding.widget.RxTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;

public class MainActivity extends AppCompatActivity {

    private EditText edittext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        edittext = (EditText) findViewById(R.id.edittext);
        debounce();
    }

    //scan操作符
    public void scan(View view) {
        Observable.just(1, 2, 3, 4, 5)
                .scan(new Func2<Integer, Integer, Integer>() {
                    @Override
                    public Integer call(Integer one, Integer two) {
                        Log.d("scan:", "one:" + one + " two:" + two);
                        return one * two;
                    }
                })
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onCompleted() {
                        Log.d("scan:", "onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Integer integer) {
                        Log.d("scan:", "onNext:" + integer);
                    }
                });

        Observable.range(1, 100)
                .scan((one, two) -> one + two)
                .subscribe(sum -> Log.d("scan:", "sum(1-100):" + sum));
    }

    public void last(View view) {
        Observable.range(1, 100)
                .scan((one, two) -> one + two)
                .takeLast(2)
                .subscribe(sum -> Log.d("scan:", "sum(1-100):" + sum));
    }

    public void takeLast(View view) {
        Observable.just(2000, 3000, 4000)
                .takeLast(2)
                .subscribe(i -> Log.d("scan:", "takeLast:" + i));
        Observable.just(2000, 3000, 4000)
                .take(2)
                .subscribe(i -> Log.d("scan:", "take:" + i));
    }

    public void buffer(View view) {
        Observable.just(2000, 3000, 4000, 5, 8, 9, 10, 111, 2)
                .buffer(2)
                .subscribe(new Action1<List<Integer>>() {
                    @Override
                    public void call(List<Integer> i) {
                        Log.d("buffer:", "buffer:" + i);
                    }
                });
    }

    public void filter(View view) {
        Observable.just("老张", "老李", "老王", "小黄", "我叫老王")
                .filter(new Func1<String, Boolean>() {
                    @Override
                    public Boolean call(String s) {
                        return s.indexOf("老王") == -1;
                    }
                })
                .subscribe(s -> Log.d("filter:", "filter:" + s));
    }

    public void map(View view) {
        Observable.just(new StringBuffer("老张"), new StringBuffer("老王"), new StringBuffer("老李"))
                .map(new Func1<StringBuffer, String>() {
                    @Override
                    public String call(StringBuffer name) {
                        return replaceName(name);
                    }
                })
                .subscribe(s -> Log.d("map:", "map:" + s));
    }

    private String replaceName(StringBuffer name) {
        int index = name.indexOf("老王");
        if (index != -1) {
            name.replace(index, 2, "**");
        }
        return name.toString();
    }


    public void flatMap(View view) {
        getNetData().flatMap(new Func1<List<String>, Observable<String>>() {
            @Override
            public Observable<String> call(List<String> strings) {
                return Observable.from(strings);
            }
        })
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        Log.d("flatMap:", "flatMap:" + s);
                    }
                });

        getNetUserId().flatMap((uid) -> getNetUserName(uid))
                .subscribe((s) -> Log.d("flatMap:", "flatMap:" + s));
    }

    public Observable<String> getNetUserName(String uid) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                if (uid.equals("1001")) {
                    subscriber.onNext("老王来了");
                } else {
                    subscriber.onNext("查无此人");
                }
                subscriber.onCompleted();
            }
        });
    }

    public Observable<String> getNetUserId() {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                subscriber.onNext("1001");
            }
        });
    }

    public Observable<List<String>> getNetData() {
        return Observable.create(new Observable.OnSubscribe<List<String>>() {
            @Override
            public void call(Subscriber<? super List<String>> subscriber) {
                List<String> list = new ArrayList<String>();
                list.add("小明");
                list.add("老王");
                list.add("老赵");
                subscriber.onNext(list);
            }
        });
    }

    public void distinct(View view) {
        Observable.just("老王", "老王", "老张", "老王")
                .distinct()
                .subscribe(s -> Log.d("distinct:", "distinct:" + s));

        Observable.just(new User("老王"), new User("老王"), new User("老张"), new User("老王"))
                .distinct(new Func1<User, String>() {
                    @Override
                    public String call(User user) {
                        return user.name;
                    }
                })
                .subscribe(s -> Log.d("distinct:", "distinct User:" + s.name));
    }

    public static class User {
        String name;

        public User(String name) {
            this.name = name;
        }
    }

    public void elementAt(View view){
        Observable.just("老王","老张","老李","小黄")
                .elementAt(2)
                .subscribe(s->Log.d("elementAt:", "elementAt:" + s));

    }

    public void first(View view){
        Observable.just("1001","1008","1009","1008")
                .first()
                .subscribe(s->Log.d("first:", "first:" + s));

        Observable.just("1001","1008","1009","1008")
                .first(new Func1<String, Boolean>() {
                    @Override
                    public Boolean call(String s) {
                        Log.d("first:", "id:" + s);
                        return s.equals("1008");
                    }
                })
                .subscribe(s->Log.d("first:", "first:" + s));
    }
    public void ignoreElements(View view){
        Observable.create(new Observable.OnSubscribe<Object>() {
            @Override
            public void call(Subscriber<? super Object> subscriber) {
                for(int i=0; i<3; i++){
                    Log.d("ignoreElements:","CallonNext,保存成功"+i);
                    subscriber.onNext("CallonNext,保存成功");
                }
                subscriber.onCompleted();
            }
        })
                .ignoreElements()
                .subscribe(new Subscriber<Object>() {
                    @Override
                    public void onCompleted() {
                        Log.d("ignoreElements:", "onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("ignoreElements:", "onError");
                    }

                    @Override
                    public void onNext(Object o) {
                        Log.d("ignoreElements:", "onNext:"+o);
                    }
                });
    }
    public void debounce(){
        RxTextView.textChanges(edittext)
                .debounce(1000,TimeUnit.MILLISECONDS)
                .subscribe(s->Log.d("debounce:", "onNext:"+s));
//        edittext.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                Log.d("debounce:", "edittext:"+s);
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//
//            }
//        });
    }
    public void join(View view){
        Observable.just("1005","1006","1008","10009")
                .join(Observable.just("10009"), new Func1<String, Observable<String>>() {
                    @Override
                    public Observable<String> call(String s) {
                        Log.d("join:", "leftSelector:"+s);
                        return Observable.just(s);
                    }
                }, new Func1<String, Observable<String>>() {
                    @Override
                    public Observable<String> call(String s) {
                        Log.d("join:", "rightSelector:"+s);
                        return Observable.just(s);
                    }
                }, new Func2<String, String, String>() {
                    @Override
                    public String call(String s, String s2) {
                        Log.d("join:", "resultSelector:"+s);
                        return s+"--"+s2;
                    }
                })
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
                        Log.d("join:", "onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("join:", "onError");
                    }

                    @Override
                    public void onNext(String s) {
                        Log.d("join:", "onNext:"+s);
                    }
                });
    }
    public void merge(View view){
        Observable<Integer> just = Observable.just(1, 2, 3);
        Observable<Integer> just2 = Observable.just(8, 9, 10);
        Observable.merge(just,just2)
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onCompleted() {
                        Log.d("merge:", "onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("merge:", "onError");
                    }

                    @Override
                    public void onNext(Integer integer) {
                        Log.d("merge:", "onNext:"+integer);
                    }
                });

    }
    public void rxCatch(View view){
        //region onErrorReturn 让Observable遇到错误时发射一个特殊的项并且正常终止。
//        Observable.create(new Observable.OnSubscribe<String>() {
//            @Override
//            public void call(Subscriber<? super String> subscriber) {
//                for(int i=0; i<5; i++){
//                    if(i%2==0){
//                        subscriber.onError(new Throwable("偶数"));
//                    }else{
//                        subscriber.onNext("i:"+i);
//                    }
//                }
//                subscriber.onCompleted();
//            }
//        })
//                .onErrorReturn(new Func1<Throwable, String>() {
//                    @Override
//                    public String call(Throwable throwable) {
//                        return throwable.getMessage();
//                    }
//                })
//                .subscribe(new Subscriber<String>() {
//                    @Override
//                    public void onCompleted() {
//                        Log.d("rxCatch:", "onCompleted");
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        Log.d("rxCatch:", "onError："+e.getMessage());
//                    }
//
//                    @Override
//                    public void onNext(String s) {
//                        Log.d("rxCatch:", "onNext："+s);
//                    }
//                });
        //endregion

        //region onErrorResumeNext 让Observable在遇到错误时开始发射第二个Observable的数据序列。
//        Observable.create(new Observable.OnSubscribe<String>() {
//            @Override
//            public void call(Subscriber<? super String> subscriber) {
//                for(int i=0; i<5; i++){
//                    if(i%2==1){
//                        subscriber.onError(new Throwable("奇数"));
//                    }else{
//                        subscriber.onNext("i:"+i);
//                    }
//                }
//                subscriber.onCompleted();
//            }
//        })
//                .onErrorResumeNext(new Func1<Throwable, Observable<? extends String>>() {
//                    @Override
//                    public Observable<? extends String> call(Throwable throwable) {
//                        return Observable.just(throwable.getMessage(),"100","200","300");
//                    }
//                })
//                .subscribe(new Subscriber<String>() {
//                    @Override
//                    public void onCompleted() {
//                        Log.d("rxCatch:", "onCompleted");
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        Log.d("rxCatch:", "onError："+e.getMessage());
//                    }
//
//                    @Override
//                    public void onNext(String s) {
//                        Log.d("rxCatch:", "onNext："+s);
//                    }
//                });
        //endregion

        //region onExceptionResumeNext 让Observable在遇到错误时继续发射后面的数据项。
        Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                for(int i=0; i<5; i++){
                    if(i%2==1){
                        throw new RuntimeException("奇数");
                    }else{
                        subscriber.onNext("i:"+i);
                    }
                }
                subscriber.onCompleted();
            }
        })
                .onExceptionResumeNext(Observable.just("111","123","666"))
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
                        Log.d("rxCatch:", "onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("rxCatch:", "onError："+e.getMessage());
                    }

                    @Override
                    public void onNext(String s) {
                        Log.d("rxCatch:", "onNext："+s);
                    }
                });
        //endregion
    }

    int i = 1;
    public void retry(View view){
        Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                Log.d("retry:", "call:第"+i+"次请求");
                if(i== 1){
                    i++;
                    subscriber.onError(new Throwable("网络异常了"));
                }
                subscriber.onNext("请求成功");
                subscriber.onCompleted();
            }
        })
                .retry(2)
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
                        Log.d("retry:", "onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("retry:", "onError:"+e.getMessage());
                    }

                    @Override
                    public void onNext(String s) {
                        Log.d("retry:", "onNext:"+s);
                    }
                });
    }
    public Observable<String> getNet(){
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                Log.d("retry:", "call:第"+i+"次请求");
                if(i== 1){
                    i++;
                    subscriber.onError(new Throwable("网络异常了"));
                }
                subscriber.onNext("请求成功");
                subscriber.onCompleted();
            }
        });
    }

}
