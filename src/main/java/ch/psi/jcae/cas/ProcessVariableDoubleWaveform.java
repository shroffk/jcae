package ch.psi.jcae.cas;

import java.util.logging.Logger;

import gov.aps.jca.CAException;
import gov.aps.jca.CAStatus;
import gov.aps.jca.Monitor;
import gov.aps.jca.cas.ProcessVariableEventCallback;
import gov.aps.jca.cas.ProcessVariableReadCallback;
import gov.aps.jca.cas.ProcessVariableWriteCallback;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBRType;
import gov.aps.jca.dbr.DBR_CTRL_Double;
import gov.aps.jca.dbr.DBR_Double;
import gov.aps.jca.dbr.DBR_TIME_Double;
import gov.aps.jca.dbr.Severity;
import gov.aps.jca.dbr.Status;
import gov.aps.jca.dbr.TIME;
import gov.aps.jca.dbr.TimeStamp;

import com.cosylab.epics.caj.cas.handlers.AbstractCASResponseHandler;
import com.cosylab.epics.caj.cas.util.FloatingDecimalProcessVariable;

/**
 * Implementation of a Channel Access Channel of the type double[]
 */
public class ProcessVariableDoubleWaveform extends FloatingDecimalProcessVariable {

	private static Logger logger = Logger.getLogger(ProcessVariableDoubleWaveform.class.getName());

	private String units = "";
	private double[] value;
	private TimeStamp timestamp = new TimeStamp();
	private short precision = 10;

	/**
	 * Constructor - Create Process Variable
	 * 
	 * @param name
	 *            Name of the process variable
	 * @param eventCallback
	 *            Callback for the process variable
	 * @param size
	 *            The array length
	 */
	public ProcessVariableDoubleWaveform(String name, ProcessVariableEventCallback eventCallback, int size) {
		super(name, eventCallback);
		this.value = new double[size];
	}

	@Override
	protected CAStatus readValue(DBR dbr, ProcessVariableReadCallback processvariablereadcallback) throws CAException {
		logger.fine(String.format("Read value from process variable - %s.", dbr.getType().getName()));

		// Determine size of the waveform returned. If the size is set in the
		// request only this this size is returned.
		System.arraycopy(this.value, 0, dbr.getValue(), 0, Math.min(value.length, dbr.getCount()));

		// Set timestamp and other flags
		if (dbr instanceof DBR_CTRL_Double) {
			DBR_CTRL_Double u = (DBR_CTRL_Double) dbr;
			u.setStatus(Status.NO_ALARM);
			u.setSeverity(Severity.NO_ALARM);
			u.setTimeStamp(this.timestamp);
			u.setPrecision(this.precision);
			u.setUnits(this.units);
		}
		else {
			DBR_TIME_Double u = (DBR_TIME_Double) dbr;
			u.setStatus(Status.NO_ALARM);
			u.setSeverity(Severity.NO_ALARM);
			u.setTimeStamp(this.timestamp);
		}

		return CAStatus.NORMAL;
	}

	@Override
	protected CAStatus writeValue(DBR dbr, ProcessVariableWriteCallback processvariablewritecallback) throws CAException {
		logger.fine(String.format("Set value to process variable - %s.", dbr.getType().getName()));

		this.value = ((DBR_Double) dbr.convert(this.getType())).getDoubleValue();

		// Post event if there is an interest
		if (interest) {
			// Set event mask
			int mask = Monitor.VALUE | Monitor.LOG;

			// Create and fill-in DBR
			DBR monitorDBR = AbstractCASResponseHandler.createDBRforReading(this);
			System.arraycopy(this.value, 0, monitorDBR.getValue(), 0, value.length);
			fillInDBR(monitorDBR);
			((TIME) monitorDBR).setStatus(Status.NO_ALARM);
			((TIME) monitorDBR).setSeverity(Severity.NO_ALARM);
			((TIME) monitorDBR).setTimeStamp(this.timestamp);

			// port event
			eventCallback.postEvent(mask, monitorDBR);
		}

		return CAStatus.NORMAL;
	}

	@Override
	public DBRType getType() {
		return DBRType.DOUBLE;
	}

	/**
	 * Returns the milliseconds (JAVA style).
	 * 
	 * @return long The milliseconds
	 */
	public long getTimeMillis() {
		return TimeHelper.getTimeMillis(this.timestamp);
	}

	/**
	 * Returns the nanosecond offset.
	 * 
	 * @return long The nanosecond
	 */
	public long getTimeNanoOffset() {
		return TimeHelper.getTimeNanoOffset(this.timestamp);
	}

	/**
	 * Get value of this process variable
	 * 
	 * @return Value of process variable
	 */
	public double[] getValue() {
		return this.value;
	}

	/**
	 * Set value of this process variable using the current time as timestamp.
	 * While setting value all registered monitors will be fired.
	 * 
	 * @param value
	 *            Value to set
	 */
	public void setValue(double[] value) {
		this.setValue(value, new TimeStamp());
	}

	/**
	 * Set value of this process variable. While setting value all registered
	 * monitors will be fired.
	 * 
	 * @param value
	 *            Value to set
	 * @param timestamp
	 *            The Timestamp
	 */
	public void setValue(double[] value, TimeStamp timestamp) {
		this.value = value;
		this.timestamp = timestamp;

		// post event if there is an interest
		if (interest)
		{
			// set event mask
			int mask = Monitor.VALUE | Monitor.LOG;

			// create and fill-in DBR
			DBR monitorDBR = AbstractCASResponseHandler.createDBRforReading(this);
			System.arraycopy(this.value, 0, monitorDBR.getValue(), 0, value.length);
			fillInDBR(monitorDBR);
			((TIME) monitorDBR).setStatus(Status.NO_ALARM);
			((TIME) monitorDBR).setSeverity(Severity.NO_ALARM);
			((TIME) monitorDBR).setTimeStamp(this.timestamp);

			// port event
			eventCallback.postEvent(mask, monitorDBR);
		}
	}

	@Override
	public int getDimensionSize(int dimension) {
		if (dimension == 0) {
			return value.length;
		}
		else {
			return 0;
		}
	}

	public void setUnits(String units) {
		this.units = units;
	}

	@Override
	public String getUnits() {
		return units;
	}

	public void setPrecision(short precision) {
		this.precision = precision;
	}

	@Override
	public short getPrecision() {
		return precision;
	}
}
