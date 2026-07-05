package by.brsm.app.service;

import by.brsm.app.dao.CounterDao;

import java.sql.SQLException;

/**
 * Выдача следующих номеров протокола и постановления.
 */
public class NumberingService {

    public static final String PROTOCOL_COUNTER = "protocol_number";
    public static final String RESOLUTION_COUNTER = "resolution_number";

    private final CounterDao counterDao;
    private final by.brsm.app.dao.ProtocolDao protocolDao;

    public NumberingService(CounterDao counterDao, by.brsm.app.dao.ProtocolDao protocolDao) {
        this.counterDao = counterDao;
        this.protocolDao = protocolDao;
    }

    public int suggestNextProtocolNumber() throws SQLException {
        return Math.max(counterDao.getValue(PROTOCOL_COUNTER), protocolDao.findMaxNumber()) + 1;
    }

    public int nextResolutionNumber() throws SQLException {
        return counterDao.next(RESOLUTION_COUNTER);
    }

    public void syncProtocolCounter(int protocolNumber) throws SQLException {
        int current = counterDao.getValue(PROTOCOL_COUNTER);
        if (protocolNumber > current) {
            counterDao.setValue(PROTOCOL_COUNTER, protocolNumber);
        }
    }
}
