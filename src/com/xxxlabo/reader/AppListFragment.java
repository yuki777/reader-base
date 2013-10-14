package com.xxxlabo.reader;

import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import com.xxxlabo.reader.RSS20Parser.Entry;

public class AppListFragment extends ListFragment {

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		String title = getString(R.string.blog_name);
		String url = getString(R.string.feed_url);
		
		new DownloadXmlTask().execute(url);
	}

	public class CardListAdapter extends ArrayAdapter {

		LayoutInflater mInflater;

		public CardListAdapter(Context context) {
			super(context, 0);
			mInflater = LayoutInflater.from(context);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.list_item_card, parent, false);
			}
			
			List entries = (List) getItem(position);
			String title = (String) entries.get(0);
			String desc = (String) entries.get(1);
			String url = (String) entries.get(2);

			TextView tv = (TextView) convertView.findViewById(R.id.title);
			tv.setText(title);

			tv = (TextView) convertView.findViewById(R.id.sub);
			desc = desc.substring(0, 100) + " ... ";
			tv.setText(desc);

//            ImageView iv = (ImageView) convertView.findViewById(R.id.icon);
//            iv.setImageDrawable(info.applicationInfo.loadIcon(packageManager));

			return convertView;
		}
	}


	// Implementation of AsyncTask used to download XML feed from stackoverflow.com.
	private class DownloadXmlTask extends AsyncTask<String, Void, List> {

		// TODO:ここよく分かっていないのでちゃんと読め
		// see... http://developer.android.com/reference/android/os/AsyncTask.html
		protected List doInBackground(String... urls) {
			try {
				return loadXmlFromNetwork(urls[0]);
			} catch (IOException e) {
				//return getResources().getString(R.string.connection_error);
			} catch (XmlPullParserException e) {
				//return getResources().getString(R.string.xml_error);
			}catch(Exception e){
				return new ArrayList();
			}
			return null;
		}

		@Override
		protected void onPostExecute(List result) {


			CardListAdapter adapter = new CardListAdapter(getActivity());
			int size = result.size();
			for(int i=0; i<size; i++){
				Entry entry = (Entry)result.get(i);
				final String title = entry.title;
				final String desc  = Html.fromHtml(entry.description).toString();
				final String url   = entry.link;
				
				List entries = new ArrayList();
				entries.add(title);
				entries.add(desc);
				entries.add(url);

				adapter.add(entries);
			}

			int padding = (int) (getResources().getDisplayMetrics().density * 8); // 8dip
			ListView listView = getListView();
			listView.setPadding(padding, 0, padding, 0);
			listView.setScrollBarStyle(ListView.SCROLLBARS_OUTSIDE_OVERLAY);
			listView.setDivider(null);

			LayoutInflater inflater = LayoutInflater.from(getActivity());
			View header = inflater.inflate(R.layout.list_header_footer, listView, false);
			View footer = inflater.inflate(R.layout.list_header_footer, listView, false);
			listView.addHeaderView(header, null, false);
			listView.addFooterView(footer, null, false);

			setListAdapter(adapter);
		}


		// Uploads XML from stackoverflow.com, parses it, and combines it with
		// HTML markup. Returns HTML string.
		private List loadXmlFromNetwork(String urlString) throws XmlPullParserException, IOException {
			InputStream stream = null;
			// Instantiate the parser
			RSS20Parser exciteBlogXMLParser = new RSS20Parser();
			List<Entry> entries = null;
			try {
				stream = downloadUrl(urlString);
				entries = exciteBlogXMLParser.parse(stream);
				// Makes sure that the InputStream is closed after the app is
				// finished using it.
			} finally {
				if (stream != null) {
					stream.close();
				} 
			}

			return entries;
		}

		// Given a string representation of a URL, sets up a connection and gets
		// an input stream.
		private InputStream downloadUrl(String urlString) throws IOException {
			URL url = new URL(urlString);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(10000 /* milliseconds */);
			conn.setConnectTimeout(15000 /* milliseconds */);
			conn.setRequestMethod("GET");
			conn.setDoInput(true);
			// Starts the query
			conn.connect();
			return conn.getInputStream();
		}
	} // private class DownloadXmlTask extends AsyncTask<String, Void, List>
} // public class AppListFragment extends ListFragment
