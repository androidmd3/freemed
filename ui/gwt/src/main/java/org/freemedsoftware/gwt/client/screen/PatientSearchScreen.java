/*
 * $Id$
 *
 * Authors:
 *      Jeff Buchbinder <jeff@freemedsoftware.org>
 *
 * FreeMED Electronic Medical Record and Practice Management System
 * Copyright (C) 1999-2012 FreeMED Software Foundation
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package org.freemedsoftware.gwt.client.screen;

import static org.freemedsoftware.gwt.client.i18n.I18nUtil._;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.freemedsoftware.gwt.client.CurrentState;
import org.freemedsoftware.gwt.client.JsonUtil;
import org.freemedsoftware.gwt.client.ScreenInterface;
import org.freemedsoftware.gwt.client.Util;
import org.freemedsoftware.gwt.client.Util.ProgramMode;
import org.freemedsoftware.gwt.client.Api.PatientInterfaceAsync;
import org.freemedsoftware.gwt.client.i18n.AppConstants;
import org.freemedsoftware.gwt.client.widget.CustomButton;
import org.freemedsoftware.gwt.client.widget.CustomTable;
import org.freemedsoftware.gwt.client.widget.CustomTable.TableRowClickHandler;
import org.freemedsoftware.gwt.client.widget.PatientWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class PatientSearchScreen extends ScreenInterface {

	protected CustomTable sortableTable = null;

	protected PatientWidget wSmartSearch = null;

	protected ListBox wFieldName = null;

	protected TextBox wFieldValue = null;

	private static List<PatientSearchScreen> patientSearchScreenList=null;
	//Creates only desired amount of instances if we follow this pattern otherwise we have public constructor as well
	public static PatientSearchScreen getInstance(){
		PatientSearchScreen patientSearchScreen=null; 
		
		if(patientSearchScreenList==null)
			patientSearchScreenList=new ArrayList<PatientSearchScreen>();
		if(patientSearchScreenList.size()<AppConstants.MAX_SEARCH_TABS)//creates & returns new next instance of PatientSearchScreen
			patientSearchScreenList.add(patientSearchScreen=new PatientSearchScreen());
		else //returns last instance of PatientSearchScreen from list 
			patientSearchScreen = patientSearchScreenList.get(AppConstants.MAX_SEARCH_TABS-1);
		return patientSearchScreen;
	}  
	
	public static boolean removeInstance(PatientSearchScreen patientSearchScreen){
		return patientSearchScreenList.remove(patientSearchScreen);
	}
	
	public PatientSearchScreen() {
		final VerticalPanel verticalPanel = new VerticalPanel();
		initWidget(verticalPanel);

		final FlexTable flexTable = new FlexTable();
		verticalPanel.add(flexTable);
		flexTable.setWidth("100%");

		final Label smartSearchLabel = new Label(_("Smart Search") + " : ");
		flexTable.setWidget(0, 0, smartSearchLabel);

		final HorizontalPanel searchHPanel = new HorizontalPanel();
		searchHPanel.setWidth("100%");
		flexTable.setWidget(0, 1, searchHPanel);
		
		wSmartSearch = new PatientWidget();
		wSmartSearch.setWidth("100%");
		wSmartSearch.addChangeHandler(new ValueChangeHandler<Integer>() {
			@Override
			public void onValueChange(ValueChangeEvent<Integer> event) {
				Integer val = ((PatientWidget) event.getSource()).getValue();
				// Log.debug("Patient value = " + val.toString());
				try {
					if (val.compareTo(new Integer(0)) != 0) {
						String ptInfo=wSmartSearch.getText();
						if(ptInfo.indexOf("]")!=-1)
							ptInfo = ptInfo.substring(0, ptInfo.indexOf("["));
						spawnPatientScreen(val, ptInfo);
						clearForm();
					}
				} catch (Exception e) {
					// Don't do anything if no patient is declared
					GWT.log("Caught exception", e);
				}
			}
		});
		addChildWidget(wSmartSearch);
		searchHPanel.add(wSmartSearch);
		

		if(CurrentState.getMainScreen().isNeedToCreate(AppConstants.PATIENT_CATEGORY,
				AppConstants.NEW_PATIENT)){
			final CustomButton newPatient = new CustomButton(_("Add New Patient"),
					AppConstants.ICON_ADD_PERSON);
			newPatient.addClickHandler(new ClickHandler() {
	
				@Override
				public void onClick(ClickEvent arg0) {
					PatientForm patientForm = PatientForm.getInstance();
					patientForm.setPrimaryFacility(CurrentState
							.getDefaultFacility());
					Util.spawnTab(AppConstants.NEW_PATIENT, patientForm);
				}
	
			});
			searchHPanel.add(newPatient);
		}
		
		
		final Label fieldSearchLabel = new Label(_("Field Search") + " : ");
		flexTable.setWidget(1, 0, fieldSearchLabel);

		wFieldName = new ListBox();
		flexTable.setWidget(1, 1, wFieldName);
		wFieldName.setVisibleItemCount(1);
		wFieldName.addItem(_("Internal ID"), "ptid");
		wFieldName.addItem(_("Social Security Number"), "ptssn");
		wFieldName.addItem(_("Drivers License"), "ptdmv");
		wFieldName.addItem(_("Email Address"), "ptemail");
		wFieldName.addItem(_("City"), "city");
		wFieldName.addItem(_("Zip/Postal Code"), "ptzip");
		wFieldName.addItem(_("Home Phone"), "pthphone");
		wFieldName.addItem(_("Work Phone"), "ptwphone");
		wFieldName.addItem(_("Age"), "age");
		wFieldName.addItem(_("First Name"), "ptfname");
		wFieldName.addItem(_("Last Name"), "ptlname");

		wFieldValue = new TextBox();
		flexTable.setWidget(2, 1, wFieldValue);
		wFieldValue.setWidth("100%");
		wFieldValue.addKeyUpHandler(new KeyUpHandler(){

			@Override
			public void onKeyUp(KeyUpEvent arg0) {
				// TODO Auto-generated method stub
				refreshSearch();
				
			}});
		
	/*	wFieldValue.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				
			}
		});*/

		final HorizontalPanel horizontalPanel = new HorizontalPanel();
		verticalPanel.add(horizontalPanel);

		sortableTable = new CustomTable();
		sortableTable.setNoItemsText(_("No patients found with the specified criteria."));
		sortableTable.setWidth("100%");
		sortableTable.addColumn(_("Last Name"), "last_name");
		sortableTable.addColumn(_("First Name"), "first_name");
		sortableTable.addColumn(_("Middle"), "middle_name");
		sortableTable.addColumn(_("Internal ID"), "patient_id");
		sortableTable.addColumn(_("Date of Birth"), "date_of_birth");
		sortableTable.addColumn(_("Age"), "age");
		sortableTable.setTableRowClickHandler(new TableRowClickHandler() {
			@Override
			public void handleRowClick(HashMap<String, String> data, int col) {
				// Get information on row...
				JsonUtil.debug("search table handle row click");
				try {
					final Integer patientId = new Integer(data.get("id"));
					final String patientName = data.get("last_name") + ", "
							+ data.get("first_name") 
							+ data.get("patient_id");
					spawnPatientScreen(patientId, patientName);
				} catch (Exception e) {
					GWT.log("Caught exception: ", e);
				}
			}
		});

		//+ " ["
		//+ data.get("date_of_birth") + "] "
		final VerticalPanel stPanel = new VerticalPanel();
		stPanel.setWidth("100%");
		stPanel.add(sortableTable);
		stPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		verticalPanel.add(stPanel);

		// Set visible focus *after* this is shown, otherwise it won't focus.
		try {
			wSmartSearch.setFocus(true);
		} catch (Exception e) {
			GWT.log("Caught exception: ", e);
		}
		Util.setFocus(wSmartSearch);
	}

	@SuppressWarnings("unchecked")
	protected void refreshSearch() {
		sortableTable.clearData();
		if (Util.getProgramMode() == ProgramMode.STUBBED) {
			HashMap<String, String> a = new HashMap<String, String>();
			a.put("last_name", "Hackenbush");
			a.put("first_name", "Hugo");
			a.put("middle_name", "Z");
			a.put("patient_id", "HACK01");
			a.put("date_of_birth", "1979-08-10");
			a.put("age", "28");
			a.put("id", "1");
			List<HashMap<String, String>> l = new ArrayList<HashMap<String, String>>();
			l.add(a);
			sortableTable.loadData((HashMap<String, String>[]) l
					.toArray(new HashMap<?, ?>[0]));
		} else if (Util.getProgramMode() == ProgramMode.JSONRPC) {
			HashMap<String, String> criteria = new HashMap<String, String>();
			criteria.put(wFieldName.getValue(wFieldName.getSelectedIndex()),
					wFieldValue.getText());

			String[] params = { JsonUtil.jsonify(criteria) };
			RequestBuilder builder = new RequestBuilder(RequestBuilder.POST,
					URL.encode(Util.getJsonRequest(
							"org.freemedsoftware.api.PatientInterface.Search",
							params)));
			try {
				builder.sendRequest(null, new RequestCallback() {
					public void onError(Request request, Throwable ex) {
						Window.alert(ex.toString());
					}

					public void onResponseReceived(Request request,
							Response response) {
						if (Util.checkValidSessionResponse(response.getText())) {
							if (200 == response.getStatusCode()) {
								HashMap<String, String>[] result = (HashMap<String, String>[]) JsonUtil
										.shoehornJson(JSONParser.parseStrict(response
												.getText()),
												"HashMap<String,String>[]");
								sortableTable.loadData(result);
							
							} else {
								Window.alert(response.toString());
							}
						}
					}
				});
			} catch (RequestException e) {
				Window.alert(e.toString());
			}
		} else {
			PatientInterfaceAsync service = null;
			try {
				service = (PatientInterfaceAsync) Util
						.getProxy("org.freemedsoftware.gwt.client.Api.PatientInterface");
			} catch (Exception e) {
				GWT.log("Caught exception: ", e);
			}

			HashMap<String, String> criteria = new HashMap<String, String>();
			criteria.put(wFieldName.getValue(wFieldName.getSelectedIndex()),
					wFieldValue.getText());

			service.Search(criteria,
					new AsyncCallback<HashMap<String, String>[]>() {
						public void onSuccess(HashMap<String, String>[] result) {
							// Log.info("found " + new
							// Integer(r.length).toString() + "
							// results for Search");
							sortableTable.loadData(result);
						}

						public void onFailure(Throwable t) {
							// Log.error("Caught exception: ", t);
						}
					});
		}
	}

	/**
	 * Create new tab for patient.
	 * 
	 * @param patient
	 */
	public void spawnPatientScreen(Integer patient, String patientName) {
		PatientScreen s = new PatientScreen();
		s.setPatient(patient);
		Util.spawnTab(patientName, s);
	}

	public void clearForm() {
		wSmartSearch.clear();
		wFieldValue.setText("");
	}
	@Override
	public void closeScreen() {
		// TODO Auto-generated method stub
		super.closeScreen();
		removeInstance(this);
	}
}
