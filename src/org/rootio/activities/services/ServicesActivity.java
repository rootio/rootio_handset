package org.rootio.activities.services;

import java.util.HashMap;
import java.util.Map.Entry;

import org.rootio.radioClient.R;
import org.rootio.services.DiagnosticsService;
import org.rootio.services.DiscoveryService;
import org.rootio.services.Notifiable;
import org.rootio.services.ProgramService;
import org.rootio.services.SMSService;
import org.rootio.services.ServiceConnectionAgent;
import org.rootio.services.ServiceInformationPublisher;
import org.rootio.services.ServiceState;
import org.rootio.services.SynchronizationService;
import org.rootio.services.TelephonyService;
import org.rootio.tools.utils.Utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

public class ServicesActivity extends Activity implements
		OnCheckedChangeListener, Notifiable {

	private HashMap<Integer, ServiceComponents> serviceComponents;

	private BroadcastIntentHandler broadCastIntentHandler;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.services);
		this.setTitle("Services");
		setupBroadcastHandling();
		setupServiceComponents();
		this.getServiceConnections();
	}
/**
 * set up a broadacst receiver for Intents thrown by various services
 */
	private void setupBroadcastHandling() {
		this.broadCastIntentHandler = new BroadcastIntentHandler(this);
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("org.rootio.services.sms.EVENT");
		intentFilter.addAction("org.rootio.services.telephony.EVENT");
		intentFilter.addAction("org.rootio.services.diagnostic.EVENT");
		intentFilter.addAction("org.rootio.services.program.EVENT");
		intentFilter.addAction("org.rootio.services.synchronization.EVENT");
		intentFilter.addAction("org.rootio.services.discovery.EVENT");
		this.registerReceiver(broadCastIntentHandler, intentFilter);
	}

	/**
	 * Define the particulars of each service
	 */
	private void setupServiceComponents() {
		this.serviceComponents = new HashMap<Integer, ServiceComponents>();
		this.serviceComponents
				.put(1,
						new ServiceComponents(
								null,
								(LinearLayout) this
										.findViewById(R.id.telephony_service_lt),
								(Switch) this
										.findViewById(R.id.telephony_service_swt),
								(TextView) this
										.findViewById(R.id.telephony_service_tv),
								new ServiceState(this, 1), new Intent(this,
										TelephonyService.class), this));
		this.serviceComponents
				.put(2,
						new ServiceComponents(
								null,
								(LinearLayout) this
										.findViewById(R.id.sms_service_lt),
								(Switch) this
										.findViewById(R.id.messaging_service_swt),
								(TextView) this
										.findViewById(R.id.messaging_service_tv),
								new ServiceState(this, 2), new Intent(this,
										SMSService.class), this));
		this.serviceComponents.put(
				3,
				new ServiceComponents(null,
						(LinearLayout) this
								.findViewById(R.id.diagnostic_service_lt),
						(Switch) this
								.findViewById(R.id.diagnostics_service_swt),
						(TextView) this
								.findViewById(R.id.diagnostics_service_tv),
						new ServiceState(this, 3), new Intent(this,
								DiagnosticsService.class), this));
		this.serviceComponents.put(
				4,
				new ServiceComponents(null,
						(LinearLayout) this
								.findViewById(R.id.program_service_lt),
						(Switch) this.findViewById(R.id.program_service_swt),
						(TextView) this.findViewById(R.id.program_service_tv),
						new ServiceState(this, 4), new Intent(this,
								ProgramService.class), this));
		this.serviceComponents.put(5,
				new ServiceComponents(null,
						(LinearLayout) this.findViewById(R.id.sync_service_lt),
						(Switch) this.findViewById(R.id.sync_service_swt),
						(TextView) this.findViewById(R.id.sync_service_tv),
						new ServiceState(this, 5), new Intent(this,
								SynchronizationService.class), this));
		this.serviceComponents
				.put(6,
						new ServiceComponents(
								null,
								(LinearLayout) this
										.findViewById(R.id.discovery_service_lt),
								(Switch) this
										.findViewById(R.id.discovery_service_swt),
								(TextView) this
										.findViewById(R.id.discovery_service_tv),
								new ServiceState(this, 6), new Intent(this,
										DiscoveryService.class), this));
	}

	@Override
	public void onStart() {
		super.onStart();

	}

	@Override
	public void onStop() {
		super.onStop();
		try {
			this.unregisterReceiver(broadCastIntentHandler);
		} catch (Exception ex) {

		}
	}

	@Override
	public void notifyServiceConnection(int serviceId) {
		ServiceInformationPublisher  service = this.serviceComponents.get(serviceId).getServiceConnectionAgent().getService();
			if (service == null) {
				this.updateDisplay(serviceId, false);
			} else {
				this.updateDisplay(serviceId, service.isRunning());
			}
	}

	/**
	 * Bind to the various services to be able to communicate with them
	 */
	private void getServiceConnections() { 
		for(Entry<Integer, ServiceComponents> serviceComponent : this.serviceComponents.entrySet())
		{
			this.bindServiceConnection(serviceComponent.getKey());
		}
	}

	@SuppressLint("NewApi")
	/**
	 * Update the display of the service to reflect the status of the service
	 * @param serviceId
	 * @param isRunning
	 */
	void updateDisplay(int serviceId, boolean isRunning) {
		View serviceLinearLayout = this.serviceComponents
				.get(serviceId).getLinearLayout();
		int paddingTop = serviceLinearLayout.getPaddingTop();
		int paddingLeft = serviceLinearLayout.getPaddingLeft();
		int paddingRight = serviceLinearLayout.getPaddingRight();
		int paddingBottom = serviceLinearLayout.getPaddingBottom();
		serviceLinearLayout.setBackgroundResource(isRunning ? R.drawable.green_background: R.drawable.pink_background);
		serviceLinearLayout.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
		this.serviceComponents.get(serviceId).getSwitch().setChecked(isRunning);
	}

	/**
	 * Stops a service whose id is supplied
	 * @param serviceId The ID of the service to be stopped
	 */
	private void stopService(int serviceId) {
		Intent serviceIntent = this.serviceComponents.get(serviceId)
				.getIntent();
		this.unbindServiceConnection(serviceId);
		this.stopService(serviceIntent);
		this.serviceComponents.get(serviceId).getServiceState()
				.setServiceState(0);
	}

	/**
	 * Unbinds from the service whose ID is supplied
	 * @param serviceId The ID of the service from which to unbind
	 */
	private void unbindServiceConnection(int serviceId) {
		try {
			this.unbindService(this.serviceComponents.get(serviceId)
					.getServiceConnectionAgent());
			this.serviceComponents.get(serviceId).setServiceConnectionAgent(
					null);
		} catch (Exception ex) // may not be bound
		{

		}
	}

	@SuppressLint("NewApi")
	/**
	 * Starts the service whose ID is supplied
	 * @param serviceId The ID of the service to start
	 */
	private void startService(int serviceId) {
		Utils.toastOnScreen("Starting "+serviceId);
		Intent serviceIntent = this.serviceComponents.get(serviceId)
				.getIntent();
		this.bindServiceConnection(serviceId);
		this.bindServiceConnection(serviceId);
		this.startService(serviceIntent);
		this.serviceComponents.get(serviceId).getServiceState()
				.setServiceState(1);
	}

	/**
	 * Binds to the service whose ID is supplied
	 * @param serviceId The ID of the service to which to bind
	 */
	private void bindServiceConnection(int serviceId) {

		if (this.serviceComponents.get(serviceId).getServiceConnectionAgent() == null) {
			this.serviceComponents.get(serviceId).setServiceConnectionAgent(
					new ServiceConnectionAgent(this, serviceId));
			if (!this.bindService(this.serviceComponents.get(serviceId)
					.getIntent(), this.serviceComponents.get(serviceId)
					.getServiceConnectionAgent(), Context.BIND_AUTO_CREATE)) {
				this.updateDisplay(serviceId, false);
			}
		}

	}

	@Override
	public void onCheckedChanged(CompoundButton button, boolean isChecked) {
		int serviceId = 0;
		switch (button.getId()) {
		case R.id.telephony_service_swt:
			serviceId = 1;
			break;
		case R.id.messaging_service_swt:
			serviceId = 2;
			break;
		case R.id.diagnostics_service_swt:
			serviceId = 3;
			break;
		case R.id.program_service_swt:
			serviceId = 4;
			break;
		case R.id.sync_service_swt:
			serviceId = 5;
			break;
		case R.id.discovery_service_swt:
			serviceId = 6;
			break;
		}

		if (isChecked) {
			this.startService(serviceId);
		} else {
			this.stopService(serviceId);
		}

	}

	@Override
	public void notifyServiceDisconnection(int serviceId) {
		this.updateDisplay(serviceId, false);

	}

	/**
	 * This class is an encapsulation and all the components that relate to it
	 * @author Jude Mukundane
	 *
	 */
	class ServiceComponents {

		private LinearLayout llt;
		private Switch swt;
		private TextView tv;
		private ServiceState svcst;
		private Intent intent;
		private ServiceConnectionAgent conn;

		ServiceComponents(ServiceConnectionAgent conn, LinearLayout llt,
				Switch swt, TextView tv, ServiceState svcst, Intent intent,
				OnCheckedChangeListener listener) {
			this.conn = conn;
			this.tv = tv;
			this.llt = llt;
			this.swt = swt;
			this.svcst = svcst;
			this.intent = intent;
			this.swt.setOnCheckedChangeListener(listener);
		}

		/**
		 * Set the ServiceConnectionAgent to be used to communicate to the service
		 * @param conn The ServiceConnectionAgent to be used to communicate to the service
		 */
		public void setServiceConnectionAgent(ServiceConnectionAgent conn) {
			this.conn = conn;
		}

		/**
		 * Get the ServiceConnectionAgent to be used to communicate to the service
		 * @return The ServiceConnectionAgent to be used to communicate to the service
		 */
		public ServiceConnectionAgent getServiceConnectionAgent() {
			return this.conn;
		}

		/**
		 * Get the text view to be used to display information about service activity
		 * @return Text view to be used to display service activity information
		 */
		public TextView getTextView() {
			return this.tv;
		}

		/**
		 * Gets the Intent to be used in communication with the service
		 * @return The Intent that will be used to communicate with the service
		 */
		public Intent getIntent() {
			return this.intent;
		}

		/**
		 * Gets the linearlayout to display status information about the service
		 * @return The linear layout that holds status information about the service
		 */
		public LinearLayout getLinearLayout() {
			return this.llt;
		}

		/**
		 * Gets the switch that is used to control the service
		 * @return Switch that is used to control this partiular service
		 */
		public Switch getSwitch() {
			return this.swt;
		}

		/**
		 * Gets the service state of the service
		 * @return ServiceState object representing the status of the service
		 */
		public ServiceState getServiceState() {
			return this.svcst;
		}
	}
}
