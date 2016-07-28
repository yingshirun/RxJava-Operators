# RxJava操作符

rxjava operators

---

> 这篇文章用来介绍一些经常会用到的Rxjava操作符，现在网上的一些介绍的资料大都是太抽象，不易于理解，看的实在是头疼，所以我这里通过个人对操作符的理解，以这些操作符的实际使用场景来介绍它们，

##变换操作
### **scan 操作符**
> Scan操作符对原始Observable发射的第一项数据应用一个函数，然后将那个函数的结果作为自己的第一项数据发射。它将函数的结果同第二项数据一起填充给这个函数来产生它自己的第二项数据。它持续进行这个过程来产生剩余的数据序列。这个操作符在某些情况下被叫做accumulator。 

看完是不是有点晕? 来个栗子:

 - 比如 求 5的阶乘 并打印出每次计算的结果

 

```java
 public void scan(View view){
        Observable.just(1,2,3,4,5)
                .scan(new Func2<Integer, Integer, Integer>() {
                    @Override
                    public Integer call(Integer one, Integer two) {
                        Log.d("scan:","one:"+one+" two:"+two);
                        return one*two;
                    }
                })
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onCompleted() {
                        Log.d("scan:","onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Integer integer) {
                        Log.d("scan:","onNext:"+integer);
                    }
                });
    }
    
```
>打印结果

>scan:: onNext:1  
>scan:: one:1 two:2  
>scan:: onNext:2  
>scan:: one:2 two:3  
>scan:: onNext:6  
>scan:: one:6 two:4  
>scan:: onNext:24  
>scan:: one:24 two:5  
>scan:: onNext:120  
>scan:: onCompleted

>打印结果分析:  
scan:: onNext:1      
>>第一次没有走scan的call 

>scan:: one:1 two:2  
>scan:: onNext:2
>>第二次第一个参数是上一次的运行结果，第二个参数是本次的数据源 所以 one:1 two:2
>>onNext = 1*2 = 2

>scan:: one:2 two:3   
scan:: onNext:6     
>>第三次  第一个参数是上一次运行的结果 2， 第二个参数是本次的发射的数据 3 所以 one:2 tow 3
>>onNext = 2*3 = 6

>scan:: one:6 two:4  
scan:: onNext:24    
>>第四次 第一个参数是上一次运行的结果 6 第二个参数是本次发射的数据 4 所以 one:6 two 4
>>onNext = 6*4 = 24

>scan:: one:24 two:5  
scan:: onNext:120   
>>第五次 第一个参数是上一次运行的结果 24 第二个参数是本次发射的数据 5 所以 one:24 two 5
>>onNext = 24*5 = 120

>scan:: onCompleted  
 完成

- 比如 求1-100的和 (这里配合[Lambda表达式](http://blog.csdn.net/ziqiang1/article/details/50824830) 可以简洁很多)

```java
        Observable.range(1,100)
                .scan((one,two)->one+two)
                .subscribe(sum->Log.d("scan:","sum(1-100):"+sum));
```
>打印结果:
scan:: sum(1-100):1  
scan:: sum(1-100):3  
...  
...   
...  
scan:: sum(1-100):4950  
scan:: sum(1-100):5050  



###**flagMap 操作符**
>FlatMap将一个发射数据的Observable变换为多个Observables，然后将它们发射的数据合并后放进一个单独的Observable

我们看个简单的例子:
>例如:从网络获取一组用户信息，然后在分别遍历每个用户的名称
>
>```java
>  public void flatMap(View view) {
>        getNetData().flatMap(new Func1<List<String>, Observable<String>>() {
>                    @Override
>                    public Observable<String> call(List<String> strings) {
>                        return Observable.from(strings);
>                    }
>                })
>                .subscribe(new Action1<String>() {
>                    @Override
>                    public void call(String s) {
>                        Log.d("flatMap:", "flatMap:" +s);
>                    }
>                });
>
>    }
>    public Observable<List<String>> getNetData() {
>        return Observable.create(new Observable.OnSubscribe<List<String>>() {
>            @Override
>            public void call(Subscriber<? super List<String>> subscriber) {
>                List<String> list = new ArrayList<String>();
>                list.add("小明");
>                list.add("老王");
>                list.add("老赵");
>                subscriber.onNext(list);
>            }
>        });
>    }
>```
打印结果:
>flatMap:: flatMap:小明
flatMap:: flatMap:老王
flatMap:: flatMap:老赵

flagMap参数分析:
>```java
flatMap(new Func1<List<String>, Observable<String>>() {
                    @Override
                    public Observable<String> call(List<String> strings) {
                        return Observable.from(strings);
                    }
                }
>```
>可以看到flatmap的参数 Func1的泛型约束有两个 分别是 `List<String>` 和 `Observable<String>`
这两个泛型分别是约束的call方法的参数和返回值类型，而`List<String>` 是上面传来的数据,
`Observable<String>` 转换后返回的数据
这里就是将`List<String>` 转为`Observable<String>`.

在来举个栗子：

>例如 我需要在接口A中 去获取我的id，然后在根据我的id去接口B去获取我的信息
>```java
>   public void flatMap(View view) {
>       getNetUserId().flatMap((uid) -> getNetUserName(uid))
>               .subscribe((s) -> Log.d("flatMap:", "flatMap:" + s));
>   }

>   public Observable<String> getNetUserName(String uid) {
>       return Observable.create(new Observable.OnSubscribe<String>() {
>           @Override
>           public void call(Subscriber<? super String> subscriber) {
>               if (uid.equals("1001")) {
>                   subscriber.onNext("老王来了");
>               } else {
>                   subscriber.onNext("查无此人");
>               }
>               subscriber.onCompleted();
>           }
>       });
>   }

>   public Observable<String> getNetUserId() {
>       return Observable.create(new Observable.OnSubscribe<String>() {
>           @Override
>           public void call(Subscriber<? super String> subscriber) {
>               subscriber.onNext("1001");
>           }
>       });
>   }
>```    

>打印结果：  
flatMap:: flatMap:老王来了  

###**map 操作符**
对Observable发射的每一项数据应用一个函数，执行变换操作
举个栗子：
>例如 现在来个需求，说老王这个名字太内涵，要屏蔽掉，凡是名称里有老王的 都要用**给他和谐了
>```java
    public void map(View view) {
        Observable.just(new StringBuffer("老张"),new StringBuffer("老王"),new StringBuffer("老李"))
                .map(new Func1<StringBuffer, String>() {
                    @Override
                    public String call(StringBuffer name) {
                        return replaceName(name);
                    }
                })
                .subscribe(s->Log.d("map:", "map:" + s));
    }
    private String replaceName(StringBuffer name){
        int index = name.indexOf("老王");
        if(index!=-1){
            name.replace(index,2,"**");
        }
        return name.toString();
    }
>```

>打印结果：  
map:: map:老张  
map:: map:**  
map:: map:老李  

>map的fun1参数和flagMap的参数意思都一样，这里是吧StringBuffer转换成String

###**buffer操作符**
定期收集Observable的数据放进一个数据包裹，然后发射这些数据包裹，而不是一次发射一个值。
举个栗子：
>```java
    public void buffer(View view) {
        Observable.just(2000, 3000, 4000,5,8,9,10,111,2)
                .buffer(2)
                .subscribe(new Action1<List<Integer>>() {
                    @Override
                    public void call(List<Integer> i) {
                        Log.d("buffer:", "buffer:" + i);
                    }
                });
    }
>```  
>打印结果：  
buffer:: buffer:[2000, 3000]  
buffer:: buffer:[4000, 5]  
buffer:: buffer:[8, 9]  
buffer:: buffer:[10, 111]  
buffer:: buffer:[2]  

>可以看到 buffer对发射的数据做了一个缓存，然后以集合的方式发射出来,每2个一组



----------


##过滤操作
###**filter操作符**
Filter操作符使用你指定的一个谓词函数测试数据项，只有通过测试的数据才会被发射。
其实就是对数据进行一次过滤，只有通过过滤条件的数据才会被发射
举个栗子:
>比如：来个需求说，老王的名字太内涵了，现在所有名字中含有老王的都不要展示了
>```java
  Observable.just("老张","老李","老王","小黄","我叫老王")
                .filter(new Func1<String, Boolean>() {
                    @Override
                    public Boolean call(String s) {
                        return s.indexOf("老王")==-1;
                    }
                })
                .subscribe(s->Log.d("filter:", "filter:" + s));
>```
打印结果:  
filter:: filter:老张  
filter:: filter:老李  
filter:: filter:小黄  

>可以看到名字中含有老王的都被过滤掉了
###distinct操作符
抑制（过滤掉）重复的数据项
看下代码:
>```java
        Observable.just("老王", "老王", "老张", "老王")
                .distinct()
                .subscribe(s -> Log.d("distinct:", "distinct:" + s));
>```
打印结果:  
distinct:: distinct:老王  
distinct:: distinct:老张  

>3个老王只打印了一个，实现了去重操作

但是对于实体类的对象怎么去重呢？
举个栗子:
>```java
> Observable.just(new User("老王"), new User("老王"), new User("老张"), new User("老王"))
                .distinct(new Func1<User, String>() {
                    @Override
                    public String call(User user) {
                        return user.name;
                    }
                })
                .subscribe(s -> Log.d("distinct:", "distinct User:" + s.name));

>  //我们的实体类对象
>  public class User {
        String name;

>        public User(String name) {
            this.name = name;
        }
 
>    }
>```
>打印结果   
distinct:: distinct User:老王   
distinct:: distinct User:老张  

>成功过滤，还是通过`Func1<User,String>` 将user对象转换成String返回 然后去比较
注意：我们这里将String当作用户类的唯一标识符


### **last 操作符**
在scan中，当我们求和时，居然打印了100条，其实我们只是要最后一次的结果，这时候就需要last操作符了
```java
    public void last(View view){
        Observable.range(1,100)
                .scan((one,two)->one+two)
                .last()
                .subscribe(sum->Log.d("scan:","sum(1-100):"+sum));
    }
```
打印结果：  
>scan:: sum(1-100):5050

这样是不是爽多了，last操作符 会只取最后一次的结果

**需要注意的是 last的位置 如果在第二行的话 打印结果会截然不同**
```java
 public void last(View view){
        Observable.range(1,100)
                .last()
                .scan((one,two)->one+two)
                .subscribe(sum->Log.d("scan:","sum(1-100):"+sum));
    }
```
打印结果:  
>scan:: sum(1-100):100  
>>因为在scan看来，上面只发射了一次数据，所以不会走call方法


### **takeLast 操作符**
说了last操作符 就要说下和last差不多的功能 takeLast了 (相对的是skipLast(n):忽略发射的最后n项)
>比如 今年第一季度的每个月花销为 2000 3000 4000 现在要获得后两个月的花销
```java
  Observable.just(2000,3000,4000)
                .takeLast(2)
                .subscribe(i->Log.d("scan:","takeLast:"+i));
```
打印结果:  
>scan:: takeLast:3000  
scan:: takeLast:4000    
takeLast(n) 会取最后发射的n个数据  

###**take 操作符**
take(n) 是取发射数据的前n个 (相对的是skip(n)操作符 跳过发射数据的前n个)
>比如 今年第一季度的每个月花销为 2000 3000 4000 现在要获得前两个月的花销
```java
  Observable.just(2000,3000,4000)
                .take(2)
                .subscribe(i->Log.d("scan:","take:"+i));
```
打印结果:  
>scan:: take:2000  
scan:: take:3000  

###**elementAt 操作符**
ElementAt操作符获取原始Observable发射的数据序列指定索引位置的数据项，然后当做自己的唯一数据发射
栗子:
>```java
        Observable.just("老王","老张","老李","小黄")
                .elementAt(2)
                .subscribe(s->Log.d("elementAt:", "elementAt:" + s));
>``` 
打印结果：  
elementAt:: elementAt:老李  

>打印下标为2的老李

RxJava还实现了elementAtOrDefault操作符。与elementAt的区别是，如果索引值大于数据项数，它会发射一个默认值（通过额外的参数指定），而不是抛出异常。但是如果你传递一个负数索引值，它仍然会抛出一个IndexOutOfBoundsException异常。

###**first 操作符**
只发射第一项（或者满足某个条件的第一项）数据
来个栗子:
>```java
        Observable.just("1001","1008","1009","1008")
                .first()
                .subscribe(s->Log.d("first:", "first:" + s));
>```
打印结果：  
first:: first:1001
只打印第一项  

在来个栗子： 满足某个条件的第一项 
>```java
Observable.just("1001","1008","1009","1008")
                .first(new Func1<String, Boolean>() {
                    @Override
                    public Boolean call(String s) {
                        Log.d("first:", "id:" + s);
                        return s.equals("1008");
                    }
                })
                .subscribe(s->Log.d("first:", "first:" + s));
>```
打印结果：  
first:: id:1001  
first:: id:1008  
first:: first:1008  

>可以看到一旦找到id为1008的就不在发射数据了

###**IgnoreElements 操作符**
如果你不关心一个Observable发射的数据，但是希望在它完成时或遇到错误终止时收到通知，你可以对Observable使用ignoreElements操作符，它会确保永远不会调用观察者的onNext()方法。
举个栗子：
>比如 我要将一组数据缓存到本地数据库，现在我只想在完成的时候通知我就行了 
>```java
>     Observable.create(new Observable.OnSubscribe<Object>() {
>            @Override
>            public void call(Subscriber<? super Object> subscriber) {
>                for(int i=0; i<3; i++){
>                    Log.d("ignoreElements:","CallonNext,保存成功"+i);
>                    subscriber.onNext("CallonNext,保存成功");
>                }
>                subscriber.onCompleted();
>            }
>        })
>                .ignoreElements()
>                .subscribe(new Subscriber<Object>() {
>                    @Override
>                    public void onCompleted() {
>                        Log.d("ignoreElements:", "onCompleted");
>                    }
>
>                    @Override
>                    public void onError(Throwable e) {
>                        Log.d("ignoreElements:", "onError");
>                    }
>
>                    @Override
>                    public void onNext(Object o) {
>                        Log.d("ignoreElements:", "onNext:"+o);
>                    }
>                });
>```
>打印结果:  
ignoreElements:: CallonNext,保存成功0   
ignoreElements:: CallonNext,保存成功1  
ignoreElements:: CallonNext,保存成功2  
ignoreElements:: onCompleted

>可以看到在call里调用了3次onNext 结果却一次也没有被调用，只有完成时调用了onCompleted,这就是ignoreElements操作符的作用

###**debounce 操作符** 
仅在过了一段指定的时间还没发射数据时才发射一个数据

>来说个实际的应用，比如我要对一组数据根据用户的输入自动去做筛选时，普通的做法是:
>```java
>        edittext.addTextChangedListener(new TextWatcher() {
>            @Override
>            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                
>            }

>            @Override
>            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d("debounce:", "edittext:"+s);
            }

>            @Override
>           public void afterTextChanged(Editable s) {

>            }
>        });
>```
然后我们输入 jack  
打印结果:  
debounce:: edittext:j  
debounce:: edittext:ja  
debounce:: edittext:jac  
debounce:: edittext:jack  

>发现数据发射频率太快，因为我实际要的是jack 然后这样每次都会去做筛选，其实前3次的筛选完全没必要，

>现在我们换成debounce操作符
>```java
 RxTextView.textChanges(edittext)
                .debounce(1000,TimeUnit.MILLISECONDS)
                .subscribe(s->Log.d("debounce:", "onNext:"+s));
```
>打印结果：  
>debounce:: onNext:jack

>这个我们通过debounce操作符设置每隔1000毫秒发射一次数据，当用户在1000毫秒内输入玩jack就会只发>射一次， 想对于传统写法 不仅优化了性能，而且代码也十分的简洁

>注：RxTextView 使用的是的RxBinding
>引入方式:`compile 'com.jakewharton.rxbinding:rxbinding:0.4.0'`



##其它操作
###**merge操作符**
使用Merge操作符你可以将多个Observables的输出合并，就好像它们是一个单个的Observable一样。
栗子：

>```java
>  Observable<Integer> just = Observable.just(1, 2, 3);
>        Observable<Integer> just2 = Observable.just(8, 9, 10);
>        Observable.merge(just,just2)
>                .subscribe(new Subscriber<Integer>() {
>                   @Override
>                   public void onCompleted() {
>                        Log.d("merge:", "onCompleted");
>                    }
>
>                    @Override
>                    public void onError(Throwable e) {
>                        Log.d("merge:", "onError");
>                    }
>
>                    @Override
>                    public void onNext(Integer integer) {
>                        Log.d("merge:", "onNext:"+integer);
>                    }
>                });
>```
打印结果：  
merge:: onNext:1  
merge:: onNext:2  
merge:: onNext:3  
merge:: onNext:8  
merge:: onNext:9  
merge:: onNext:10  
merge:: onCompleted  

###**catch 操作符**
Catch操作符拦截原始Observable的onError通知，将它替换为其它的数据项或数据序列，让产生的Observable能够正常终止或者根本不终止。

>RxJava将Catch实现为三个不同的操作符：

>1. onErrorReturn  
>让Observable遇到错误时发射一个特殊的项并且正常终止。
>
>2. onErrorResumeNext  
>让Observable在遇到错误时开始发射第二个Observable的数据序列。
>3. onExceptionResumeNext  
>让Observable在遇到错误时继续发射后面的数据项。

我们首先来看下第一个
####1.onErrorReturn
栗子：
>```java
Observable.create(new Observable.OnSubscribe<String>() {
>            @Override
>            public void call(Subscriber<? super String> subscriber) {
                for(int i=0; i<5; i++){
                    if(i%2==0){
                        subscriber.onError(new Throwable("偶数"));
                    }else{
                        subscriber.onNext("i:"+i);
                    }
                }
                subscriber.onCompleted();
            }
        })
                .onErrorReturn(new Func1<Throwable, String>() {
>                    @Override
>                    public String call(Throwable throwable) {
                        return throwable.getMessage();
                    }
                })
                .subscribe(new Subscriber<String>() {
>                    @Override
>                    public void onCompleted() {
                        Log.d("rxCatch:", "onCompleted");
                    }

>                    @Override
>                    public void onError(Throwable e) {
                        Log.d("rxCatch:", "onError："+e.getMessage());
                    }

 >                   @Override
 >                   public void onNext(String s) {
                        Log.d("rxCatch:", "onNext："+s);
                    }
                });
>```
打印结果为:  
rxCatch:: onNext：偶数  
rxCatch:: onCompleted  

>可以看到 使用 onErrorReturn对onError类型进行了转换，所以最后的subscribe里没有走onError方法，而是走了onNext方法，但是由于才发射数据时走了onError方法，所以数据序列的发射被中止了

####2.onErrorResumeNext
栗子：
>```java
Observable.create(new Observable.OnSubscribe<String>() {
>            @Override
            public void call(Subscriber<? super String> subscriber) {
                for(int i=0; i<5; i++){
                    if(i%2==1){
                        subscriber.onError(new Throwable("奇数"));
                    }else{
                        subscriber.onNext("i:"+i);
                    }
                }
                subscriber.onCompleted();
            }
        })
                .onErrorResumeNext(new Func1<Throwable, Observable<? extends String>>() {
>                   @Override
                    public Observable<? extends String> call(Throwable throwable) {
                        return Observable.just(throwable.getMessage(),"100","200","300");
                    }
                })
                .subscribe(new Subscriber<String>() {
>                    @Override
                    public void onCompleted() {
                        Log.d("rxCatch:", "onCompleted");
                    }

>                    @Override
                    public void onError(Throwable e) {
                        Log.d("rxCatch:", "onError："+e.getMessage());
                    }

 >                   @Override
                    public void onNext(String s) {
                        Log.d("rxCatch:", "onNext："+s);
                    }
                });
>```
打印结果:  
rxCatch:: onNext：i:0  
rxCatch:: onNext：奇数  
rxCatch:: onNext：100  
rxCatch:: onNext：200  
rxCatch:: onNext：300  
rxCatch:: onCompleted  

>可以看到当发射第一个数据序列时，遇到onError后，就进到OnErrorResumeNext任务里 执行第二个数据序列的发射

####3.onExceptionResumeNext
栗子：
>```java
>Observable.create(new Observable.OnSubscribe<String>() {
>            @Override
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
>                    @Override
                    public void onCompleted() {
                        Log.d("rxCatch:", "onCompleted");
                    }

>                    @Override
                    public void onError(Throwable e) {
                        Log.d("rxCatch:", "onError："+e.getMessage());
                    }

>                    @Override
                    public void onNext(String s) {
                        Log.d("rxCatch:", "onNext："+s);
                    }
                });
>```

>打印结果：  
rxCatch:: onNext：i:0  
rxCatch:: onNext：111  
rxCatch:: onNext：123  
rxCatch:: onNext：666  
rxCatch:: onCompleted  

>可以看到 当i为奇数时，我们抛了个异常，然后就走onExceptionResumeNext 去继续发射第二个数据序列

注意：`onExceptionResumeNext`和`onErrorResumeNext`的区别，前者是抛异常的时候会触发，而后者是发生错误的时候触发，java的异常分为错误（error）和异常（exception）两种.

###**retry 操作符**
如果原始Observable遇到错误，重新订阅它期望它能正常终止

例如：网络请求时，如果出现错误添加重试次数
>```java
    int i = 1;
    public void retry(View view){
        Observable.create(new Observable.OnSubscribe<String>() {
>            @Override
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
>                    @Override
                    public void onCompleted() {
                        Log.d("retry:", "onCompleted");
                    }

>                    @Override
                    public void onError(Throwable e) {
                        Log.d("retry:", "onError:"+e.getMessage());
                    }

>                    @Override
                    public void onNext(String s) {
                        Log.d("retry:", "onNext:"+s);
                    }
                });
    }
>```
打印结果:  
retry:: call:第1次请求  
retry:: call:第2次请求   
retry:: onNext:请求成功  
retry:: onCompleted  

>可以看到一共进行了两次请求，第一次请求失败的没有走onError方法

>retry(n)操作符的作用就是当发射数据的过程中出现了错误，会进行n次的重试.


----------


##[源码下载](https://github.com/yingshirun/RxJava-Operators)
