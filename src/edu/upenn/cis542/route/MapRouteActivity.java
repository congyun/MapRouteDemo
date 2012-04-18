package edu.upenn.cis542.route;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import edu.upenn.cis542.route.Road;
import edu.upenn.cis542.route.RoadProvider;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class MapRouteActivity extends MapActivity {

        LinearLayout linearLayout;
        MapView mapView;
        private String   i_type;
        private Road mRoad;
        private PlacesList mList;
        Drawable s_marker;
        Drawable d_marker;
        Drawable i_marker;
        
        /*Params that need to be passed from main program*/
        
      
        RoadProvider.Mode mode;
        double fromLat, fromLon, toLat, toLon;

        @Override
        public void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.main);
                mapView = (MapView) findViewById(R.id.mapview);
                s_marker = getResources().getDrawable(R.drawable.marker_a);
                d_marker = getResources().getDrawable(R.drawable.marker_b);
                i_marker = getResources().getDrawable(R.drawable.heart);
                mapView.setBuiltInZoomControls(true);
               
                
                fromLat = 39.952881;
                fromLon = -75.209437;
                toLat = 39.952759;
                toLon = -75.192776;
                mode = RoadProvider.Mode.BICYCLING;
                i_type = "food";
 
              
                new Thread() {
                        @Override
                        public void run() {
                                
                                String url = RoadProvider
                                                .getUrl(fromLat, fromLon, toLat, toLon,mode);
                                InputStream is = getConnection(url);
                                mRoad = RoadProvider.getRoute(is);
                                mHandler.sendEmptyMessage(0);
                        }
                }.start();
                
                SearchPlaces search = new SearchPlaces();
                mList = search.getNearByPlaces(fromLat, fromLon, i_type);
        		Log.v("SearchPlaces", String.valueOf(mList.results.size()));
        		for(int i = 0; i < mList.results.size(); i++)
        		{
        			Log.v("SearchPlaces", mList.results.get(i).toString());
        		}
        }

        Handler mHandler = new Handler() {
                public void handleMessage(android.os.Message msg) {
                        TextView textView = (TextView) findViewById(R.id.description);
                        textView.setText(mRoad.mName + " " + mRoad.mDescription);
                        MapOverlay mapOverlay = new MapOverlay(mRoad, mList,mapView, s_marker,d_marker, i_marker, fromLat, fromLon, toLat,toLon);
                        List<Overlay> listOfOverlays = mapView.getOverlays();
                        listOfOverlays.clear();
                        listOfOverlays.add(mapOverlay);
                        mapView.invalidate();
                };
        };

        private InputStream getConnection(String url) {
                InputStream is = null;
                try {
                        URLConnection conn = new URL(url).openConnection();
                        is = conn.getInputStream();
                } catch (MalformedURLException e) {
                        e.printStackTrace();
                } catch (IOException e) {
                        e.printStackTrace();
                }
                return is;
        }

        @Override
        protected boolean isRouteDisplayed() {
                return false;
        }
}

class MapOverlay extends com.google.android.maps.Overlay {
        Road mRoad;
        PlacesList mList;
        ArrayList<GeoPoint> mPoints;
        Drawable sMarker;
        Drawable dMarker;
        Drawable iMarker;
        double m_fromLat;
        double m_fromLon;
        double m_toLat;
        double m_toLon;

        public MapOverlay(Road road, PlacesList list, MapView mv, Drawable s_marker, Drawable d_marker, Drawable i_marker, double fromLat, double fromLon, double toLat, double toLon) {
                mRoad = road;
                mList = list;
                sMarker = s_marker;
                dMarker = d_marker;
                iMarker = i_marker;
                m_fromLat = fromLat;
                m_toLat = toLat;
                m_fromLon = fromLon;
                m_toLon = toLon;
                
                if (road.mRoute.length > 0) {
                        mPoints = new ArrayList<GeoPoint>();
                        for (int i = 0; i < road.mRoute.length; i++) {
                                mPoints.add(new GeoPoint((int) (road.mRoute[i][1] * 1000000),
                                                (int) (road.mRoute[i][0] * 1000000)));
                        }
                        int moveToLat = (mPoints.get(0).getLatitudeE6() + (mPoints.get(
                                        mPoints.size() - 1).getLatitudeE6() - mPoints.get(0)
                                        .getLatitudeE6()) / 2);
                        int moveToLong = (mPoints.get(0).getLongitudeE6() + (mPoints.get(
                                        mPoints.size() - 1).getLongitudeE6() - mPoints.get(0)
                                        .getLongitudeE6()) / 2);
                        GeoPoint moveTo = new GeoPoint(moveToLat, moveToLong);

                        MapController mapController = mv.getController();
                        mapController.animateTo(moveTo);
                        mapController.setZoom(7);
                }
        }

        @Override
        public boolean draw(Canvas canvas, MapView mv, boolean shadow, long when) {
                super.draw(canvas, mv, shadow);
                drawPath(mv, canvas);
                drawMarker(mv, canvas);
                return true;
        }

        private void drawMarker(MapView mv, Canvas canvas) {
        	/*draw route markers */
        	 GeoPoint s_p = new GeoPoint( (int) (m_fromLat * 1E6), (int) (m_fromLon * 1E6));
        	 Point s_screenPts = new Point();
             mv.getProjection().toPixels(s_p, s_screenPts);
             sMarker.setBounds(s_screenPts.x-10, s_screenPts.y-10, s_screenPts.x+10, s_screenPts.y+10);
             sMarker.draw(canvas);
        	// Log.v("DrawMarker", m_toLat+ " "+m_toLon);
             GeoPoint d_p = new GeoPoint( (int) (m_toLat * 1E6), (int) (m_toLon * 1E6));
            // Log.v("DrawMarker", d_p.getLatitudeE6()+ " "+d_p.getLongitudeE6());
        	 Point d_screenPts = new Point();
             mv.getProjection().toPixels(d_p, d_screenPts);
             dMarker.setBounds(d_screenPts.x-10, d_screenPts.y-10, d_screenPts.x+10, d_screenPts.y+10);
            
             dMarker.draw(canvas);         
             
             /*draw points of interest markers*/
             ArrayList<GeoPoint> i_plist = new ArrayList<GeoPoint>();
             for(int i = 0; i < mList.results.size(); i++)
             {
            	 GeoPoint i_p = new GeoPoint( (int) (mList.results.get(i).latitude * 1E6), (int) (mList.results.get(i).longtitude * 1E6));
            	 Point i_screenPts = new Point();
                 mv.getProjection().toPixels(i_p, i_screenPts);
                 iMarker.setBounds(i_screenPts.x-10, i_screenPts.y-10, i_screenPts.x+10, i_screenPts.y+10);
                 iMarker.draw(canvas);     
                 
       
             }
             
             
                
		}

		public void drawPath(MapView mv, Canvas canvas) {
                int x1 = -1, y1 = -1, x2 = -1, y2 = -1;
                Paint paint = new Paint();
                paint.setColor(Color.BLUE);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(3);
                for (int i = 0; i < mPoints.size(); i++) {
                        Point point = new Point();
                        mv.getProjection().toPixels(mPoints.get(i), point);
                        x2 = point.x;
                        y2 = point.y;
                        if (i > 0) {
                                canvas.drawLine(x1, y1, x2, y2, paint);
                        }
                        x1 = x2;
                        y1 = y2;
                }
        }
		
		/*Response when tap a marker on screen*/
		public boolean onTap(GeoPoint p, MapView mapView)
		{
	 
			String i_name = null;
			String i_vicinity = null;
			double i_rating;
			
			for(int i = 0; i < mList.results.size()-3; i++)
            {
				GeoPoint dp = new GeoPoint((int) (mList.results.get(i).latitude *1E6), (int)(mList.results.get(i).longtitude *1E6));
				if(dp.equals(p))
				{
					System.out.println("hit!");
					i_name = mList.results.get(i).name;
					i_vicinity = mList.results.get(i).vicinity;
					i_rating = mList.results.get(i).rating;
					
					SimpleItemizedOverlay itemizedOverlay;
		            itemizedOverlay = new SimpleItemizedOverlay(iMarker, mapView);
		      		OverlayItem overlayItem = new OverlayItem(p, i_name, i_vicinity); 
		      		itemizedOverlay.addOverlay(overlayItem);
		      		List<Overlay> listOfOverlays = mapView.getOverlays();
		      		listOfOverlays.add(itemizedOverlay);
		      		mapView.invalidate();
				}
            }
			 
			return false;
		}
}