# BitmapUtils
a simple bitmap utils for android bitmap in a simple way useless memory and other operations

## version v1.0
	init utils code and same sample activity

## description
	1、this is a utils tool for decode bitmap with same simple method, get the better memory than origin.
	which using simpleSize and createScaleBitmap.
	
	2、there are some operations for save bitmap to file.
	
	3、there are some useful operations for bitmap, like oval bitmap, reflected bitmap, corner bitmap and Gauusian Blur.
	
### utils tool description
	![](https://github.com/minwalker/BitmapUtils/raw/master/BitmapUtils/screen_capture/bitmap_log.png)
	above image is the log of BitmapUtilsActivity in SampleSize but not createScaleBitmap and using createScaleBitmap, we 
	found just in sampleSize can get less memory than origin bitmap, but it's width and height is not the same with our 
	target (200,200), sampleSize can only be close to the target(some time is same), so most time it will be big than our
	target.
	
	to solve this problem, why not decode size again, so we find the createScaleBitmap, and test found decode again with it
	can get same with the target(200,200), and get the better meomery.but don't forget to recycle the bitmap of this 
	operation which never use again, and you need to judge whether the origin bitmap is the same as createScaleBitmap, this
	same times happend to origin bitmap is the same size as createScaleBitmap.
	
	finaly, we get the below resuly:
	![](https://github.com/minwalker/BitmapUtils/raw/master/BitmapUtils/BitmapUtils/screen_capture/bitmap_utils_page.png)
	
	
### operation for save bitmap to file
	android support same type for bitmap save to file, but different type takes different spaces, the result is as below:
	![](https://github.com/minwalker/BitmapUtils/raw/master/BitmapUtils/BitmapUtils/screen_capture/bitmap_save_pager.png)
	
	as google's webp type is the best, can using in web app, and same cache using in local.
	
### useful operations for bitmap
	finaly, there is same useful operations for bitmap, most is using the canvas and bitmap to draw again get the result 
	you want.
	![](https://github.com/minwalker/BitmapUtils/raw/master/BitmapUtils/BitmapUtils/screen_capture/bitmap_corner_operation_page.png)
