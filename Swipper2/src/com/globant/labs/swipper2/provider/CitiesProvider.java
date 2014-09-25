package com.globant.labs.swipper2.provider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;

import com.globant.labs.swipper2.SwipperApp;
import com.globant.labs.swipper2.models.City;
import com.globant.labs.swipper2.models.Country;
import com.globant.labs.swipper2.models.State;
import com.globant.labs.swipper2.repositories.CityRepository;
import com.globant.labs.swipper2.repositories.CountryRepository;
import com.globant.labs.swipper2.repositories.StateRepository;
import com.strongloop.android.loopback.RestAdapter;
import com.strongloop.android.loopback.callbacks.ListCallback;

public class CitiesProvider {

	protected CityRepository mCityRepository;
	protected StateRepository mStateRepository;
	protected CountryRepository mCountryRepository;
	
	protected Map<String, City> mCities;
	protected Map<String, State> mStates;
	protected Map<String, Country> mCountries;
	
	protected ListCallback<City> mCitiesCallback;
	protected ListCallback<State> mStatesCallback;
	protected ListCallback<Country> mCountriesCallback;
	
	protected CitiesCallback mCallback;
	protected boolean mReady;
	
	public CitiesProvider(Context context) {
		mReady = false;
		
		RestAdapter restAdapter = ((SwipperApp) context.getApplicationContext()).getRestAdapter();
		
		mCityRepository = restAdapter.createRepository(CityRepository.class);
		mStateRepository = restAdapter.createRepository(StateRepository.class);
		mCountryRepository = restAdapter.createRepository(CountryRepository.class);
		
		mCities = new HashMap<String, City>();
		mStates = new HashMap<String, State>();
		mCountries = new HashMap<String, Country>();
		
		mCitiesCallback = new ListCallback<City>() {
			@Override
			public void onSuccess(List<City> cities) {
				for(City c: cities) {
					c.setState(mStates.get(c.getStateId()));
					mCities.put(c.getId(), c);
				}
				
				mReady = true;

				if(mCallback != null) {
					mCallback.citiesLoaded();
				}
			}

			@Override
			public void onError(Throwable t) {
				mCallback.citiesError(t);
			}
		};
		
		mStatesCallback = new ListCallback<State>() {
			@Override
			public void onSuccess(List<State> states) {
				for(State s: states) {
					s.setCountry(mCountries.get(s.getCountryId()));
					mStates.put(s.getId(), s);
				}
				mCityRepository.findAll(mCitiesCallback);
			}

			@Override
			public void onError(Throwable t) {
				mCallback.citiesError(t);
			}
		};
		
		mCountriesCallback = new ListCallback<Country>() {
			@Override
			public void onSuccess(List<Country> countries) {
				for(Country c: countries) {
					mCountries.put(c.getId(), c);
				}
				mStateRepository.findAll(mStatesCallback);
			}

			@Override
			public void onError(Throwable t) {
				mCallback.citiesError(t);
			}
		};
	}
	
	public void setCitiesCallback(CitiesCallback callback) {
		mCallback = callback;
	}
	
	public void loadCities() {
		mCountryRepository.findAll(mCountriesCallback);
	}
	
	public boolean isReady() {
		return mReady;
	}
	
	public City getCity(String id) {
		return mCities.get(id);
	}

	public int getCount() {
		return mCities.size();
	}
	
	public interface CitiesCallback {
		public void citiesLoaded();
		public void citiesError(Throwable t);
	}
}
