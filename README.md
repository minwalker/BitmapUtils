# BitmapUtils
a simple bitmap utils for android bitmap in a simple way useless memory and other operations

## version v1.0
	1、init utils code and same sample activity.
	
	2、init a app store sample by using okhttp and IntentService.

## description
	1、this is a utils tool for decode bitmap with same simple method, get the better memory than origin.
	which using simpleSize and createScaleBitmap.
	
	2、there are some operations for save bitmap to file.
	
	3、there are some useful operations for bitmap, like oval bitmap, reflected bitmap, corner bitmap and
	Gauusian Blur.
	
	4、we use a app store sample to get remote image and cache local, memory, and how to get remote apk
	save local and install.
	
![](/BitmapUtils/screen_capture/http_app_activity.png)![](/BitmapUtils/screen_capture/http_app_info_activity.png)

![](/BitmapUtils/screen_capture/operation_http_three.png)![](/BitmapUtils/screen_capture/operation_http_four.png)
	
## utils tool description
![](/BitmapUtils/screen_capture/bitmap_log.png)

	above image is the log of BitmapUtilsActivity in SampleSize but not createScaleBitmap and using 
	createScaleBitmap, we found just in sampleSize can get less memory than origin bitmap, but it's
	width and height is not the same with our target (200,200), sampleSize can only be close to the
	target(some time is same), so most time it will be big than our target.
	
	to solve this problem, why not decode size again, so we find the createScaleBitmap, and test found
	decode again with it can get same with the target(200,200), and get the better meomery.but don't 
	forget to recycle the bitmap of this operation which never use again, and you need to judge 
	whether the origin bitmap is the same as createScaleBitmap, this same times happend to origin
	bitmap is the same size as createScaleBitmap.
	
	finaly, we get the below resuly:
![](/BitmapUtils/screen_capture/bitmap_utils_page.png)
	
	
## operation for save bitmap to file
	android support same type for bitmap save to file, but different type takes different spaces, 
	the result is as below:
![](/BitmapUtils/screen_capture/bitmap_save_pager.png)
	
	as google's webp type is the best, can using in web app, and same cache using in local.
	
## useful operations for bitmap
	there is same useful operations for bitmap, most is using the canvas and bitmap to 
	draw again get the result you want.
![](/BitmapUtils/screen_capture/bitmap_corner_operation_page.png)

## app store sample
	final, we use OkHttp and our utils to make a app store sample. we make:
	1、perpare remote data, data format as below(because i don't have remote compute, so i just make 
	a local json file inassets instealled, you can change to you remote compute with the same json 
	format is ok)
	
	"wangzhe":{
    		"name":"王者荣耀",
    		"ver":"1.33.1.23",
    		"pk_name":"com.tencent.tmgp.sgame",
    		"img_url":"http://1.pic.pc6.com/thumb/up/2015-10/2015102491743_160_160.png",
    		"desr":"王者荣耀是一款大型对战MOBA手游，由腾讯最新打造，5V5经典地图，适合喜欢团战的朋友，重度微操，
		原汁原味的团体对战体验，英雄策略搭配，实力操作公平对战，回归MOBA初心!",
    		"app_url":"https://gg.0006266.com/31300401407/9280001/72300262423",
    		"app_size":"792.1M",
    		"app_rate":"4.8",
    		"app_type":"3",
    		"img_list":[
      			"http://thumb11.jfcdns.com/thumb/2017-10/bce59f02e33b541a_600_566.jpeg",
      			"http://thumb12.jfcdns.com/thumb/2017-10/bce59f02e33d764b_600_566.jpeg",
      			"http://thumb11.jfcdns.com/thumb/2017-10/bce59f02e3400b77_600_566.jpeg",
      			"http://thumb12.jfcdns.com/thumb/2017-10/bce59f02e341e4bb_600_566.jpeg"
    			]
  		}
		
	we using JsonReader and our method readDataWithTag(JsonReader jsReader, String name, AppInfoData data) 
	to get then into AppInfoData for use to save the info of every json object.
	
	
	2、init OkHttp and cache logic for get remote image and apk.both HttpActivity, AppListActivity and
	AppInfoActivity need this logic, so we make a BaseHttpActivity as the base class of then.
	
	we create a LruCache for memory cache, and make loadRemoteOrLocalImage method to decide to get 
	local image or remote image, when there is a local file we get it into Lrucache, otherwise we 
	get remote by Okhttp and save it local for next time used, for we don't want use to know our 
	file name is our remote url, we using MD5 decode name to avoid this.
	
	String path_md5 = FileUtils.ParseMd5(url);
            File file = new File("/storage/emulated/0/my_app_cache/" + path_md5 + ".webp");
            Log.d(TAG,"loadRemoteImage file: "+(file!=null?file.exists():null));
            if (file!=null && file.exists()) {
                getLocalImage(url,path_md5,parent,width,height);
                return;
            } else {
                getRemoteImage(url,parent,width,height,corner);
            }
	    
	3、we create a IntentService(HttpService) for get the download apk task for no block main thread,
	we reforward a respone of OkHttp to get the download progress by using a Interceptor and google's
	ProgressResponseBody.
	
	final ProgressResponseBody.ProgressListener mProgressListener = new ProgressResponseBody.ProgressListener() {
                @Override
                public void update(long bytesRead, long contentLength, boolean done) {
                    int progress = (int)(100*(bytesRead*1.0f/contentLength));
                    if(mNotificationManager!=null) {
                        updateProgress(progress);
                        if(curProgress == 100) {
                            CancleNotification();
                        }
                    }
                }
            };
            Interceptor interceptor = new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Response originalResponse = chain.proceed(chain.request());
                    return originalResponse.newBuilder()
                            .body(new ProgressResponseBody(originalResponse.body(),mProgressListener))
                            .build();
                }
            };

            mClient = new OkHttpClient.Builder()
                    .connectTimeout(10000, TimeUnit.MILLISECONDS)
                    .addNetworkInterceptor(interceptor)
                    .build();
	
	we send the broadcast to refresh our ui through updateProgress(int progress) method, to reduce
	recycle update, we need to add curProgress, make sure only progress change can refresh, and
	only can update 100 times as most.
	
	private void updateProgress(int progress) {
          if(mNotificationManager!=null && mBuilder!=null) {
            if (curProgress < progress) {
                if(progress == 100) {
                    mBuilder.setContentTitle(getApplication().getResources()
		    .getString(R.string.notif_download_complete_title));
                }
                mBuilder.setProgress(100, progress, false);
                mNotificationManager.notify(100, mBuilder.build());
                Intent mProgressIntent = new Intent();
                mProgressIntent.setAction("com.http.progress.refresh");
                mProgressIntent.putExtra("app_url",current_url);
                mProgressIntent.putExtra("progress",progress);
                sendBroadcast(mProgressIntent);
                curProgress = progress;
            }
        }
    }
    
    4、we use startInstall(File file) of HttpService to install the download apk.
    
    private void startInstall(File file){
        try {
            Intent apk_intent = new Intent(Intent.ACTION_VIEW);
            apk_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            apk_intent.addCategory(Intent.CATEGORY_DEFAULT);
            if (Build.VERSION.SDK_INT >= 24) {
                Uri apkUri = FileProvider
		.getUriForFile(getApplicationContext(), "com.ice.bitmaputils.fileprovider", file);
                apk_intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                apk_intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            } else {
                apk_intent.setDataAndType(Uri.fromFile(file),
                        "application/vnd.android.package-archive");
            }
            getApplication().startActivity(apk_intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
     finally, our app store sample work like below:
![](/BitmapUtils/screen_capture/operation_http_one.gif)

![](/BitmapUtils/screen_capture/operation_http_two.gif)
