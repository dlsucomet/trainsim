package com.trainsimulation.model.db;

import com.trainsimulation.model.core.environment.TrainSystem;
import com.trainsimulation.model.core.environment.trainservice.passengerservice.stationset.Station;
import com.trainsimulation.model.core.environment.trainservice.passengerservice.trainset.Train;
import com.trainsimulation.model.db.entity.*;
import com.trainsimulation.model.utility.Schedule;
import com.trainsimulation.model.utility.TrainSystemInformation;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.ArrayList;
import java.util.List;

public class DatabaseQueries {
    private static final String GET_TRAIN_SYSTEMS
            = "" +
            "FROM TrainSystemsEntity AS ts " +
            "ORDER BY ts.name";

    private static final String GET_STATIONS_FROM_TRAIN_SYSTEM
            = "" +
            "SELECT s " +
            "FROM StationsEntity AS s " +
            "INNER JOIN s.trainSystemsByTrainSystem AS ts " +
            "WHERE ts.name = :train_system_name " +
            "ORDER BY s.sequence";

    private static final String GET_TRAINS_FROM_TRAIN_SYSTEM
            = "" +
            "SELECT t " +
            "FROM TrainsEntity AS t " +
            "INNER JOIN t.trainSystemsByTrainSystem AS ts " +
            "WHERE ts.name = :train_system_name";

    private static final String GET_TRAIN_CARRIAGES_FROM_TRAIN
            = "" +
            "SELECT tc " +
            "FROM TrainCarriagesEntity AS tc " +
            "INNER JOIN tc.trainsByTrain AS t " +
            "WHERE t.id = :train_id";

    private static final String GET_SPUR_DEPOT_SEGMENT_FROM_TRAIN_SYSTEM
            = "" +
            "SELECT d " +
            "FROM DepotsEntity AS d " +
            "INNER JOIN d.trainSystemsByTrainSystem AS ts " +
            "WHERE ts.name = :train_system_name";

    private static final String GET_END_SEGMENTS_FROM_TRAIN_SYSTEM
            = "" +
            "SELECT es " +
            "FROM EndSegmentsEntity AS es " +
            "INNER JOIN es.trainSystemsByTrainSystem AS ts " +
            "WHERE ts.name = :train_system_name " +
            "ORDER BY es.isNorthEnd";

    private static final String GET_PEAK_HOURS_FROM_PLATFORM
            = "" +
            "FROM PeakHoursEntity AS ph " +
            "INNER JOIN ph.platformsByPlatform AS p " +
            "WHERE p.id = :platform_id";

    DatabaseQueries() {
    }

    // Retrieve all train systems
    public static List<TrainSystemInformation> getTrainSystems(DatabaseInterface databaseInterface) {
        List<TrainSystemInformation> trainSystemInformations = new ArrayList<>();
        Transaction transaction = null;

        try (Session session = databaseInterface.createSession()) {
            transaction = session.beginTransaction();

            // Retrieve list of train systems
            List trainSystemsEntities = session.createQuery(DatabaseQueries.GET_TRAIN_SYSTEMS).list();

            // Add each train system into the list
            for (Object trainSystemsEntity : trainSystemsEntities) {
                trainSystemInformations.add(new TrainSystemInformation((TrainSystemsEntity) trainSystemsEntity));
            }

            transaction.commit();
        } catch (HibernateException exception) {
            if (transaction != null) {
                transaction.rollback();
            }

            exception.printStackTrace();
        }

        return trainSystemInformations;
    }

    // Retrieve all stations of a given train system
    public static List<Station> getStations(DatabaseInterface databaseInterface, TrainSystem trainSystem) {
        List<Station> stations = new ArrayList<>();
        Transaction transaction = null;

        try (Session session = databaseInterface.createSession()) {
            transaction = session.beginTransaction();

            // Retrieve list of stations
            Query query = session.createQuery(DatabaseQueries.GET_STATIONS_FROM_TRAIN_SYSTEM);
            query.setParameter("train_system_name", trainSystem.getTrainSystemInformation().getName());

            List stationsEntities = query.list();

            // Add each station into the list
            for (Object stationsEntity : stationsEntities) {
                stations.add(new Station(trainSystem, (StationsEntity) stationsEntity));
            }

            transaction.commit();
        } catch (HibernateException exception) {
            if (transaction != null) {
                transaction.rollback();
            }

            exception.printStackTrace();
        }

        // Initialize the layouts of the stations in the list
        trainSystem.initializeStationLayouts(stations);

        return stations;
    }

    // Retrieve the spur depot segment length of a given train system
    public static Integer getSpurDepotSegmentLength(DatabaseInterface databaseInterface,
                                                    TrainSystemInformation trainSystemInformation) {
        Integer spurDepotSegmentLength = null;
        Transaction transaction = null;

        try (Session session = databaseInterface.createSession()) {
            transaction = session.beginTransaction();

            // Retrieve spur depot segment length
            Query query = session.createQuery(DatabaseQueries.GET_SPUR_DEPOT_SEGMENT_FROM_TRAIN_SYSTEM);
            query.setParameter("train_system_name", trainSystemInformation.getName());

            List depotEntities = query.list();

            // Only one spur depot segment must be returned
            assert depotEntities.size() == 1 : "Not one spur depot segments have been returned";

            DepotsEntity depotsEntity = (DepotsEntity) depotEntities.get(0);
            spurDepotSegmentLength = (int) depotsEntity.getLength();

            transaction.commit();
        } catch (HibernateException exception) {
            if (transaction != null) {
                transaction.rollback();
            }

            exception.printStackTrace();
        }

        return spurDepotSegmentLength;
    }

    // Retrieve the end segment information of a given train system
    public static List<EndSegmentsEntity> getEndSegmentInformation(DatabaseInterface databaseInterface,
                                                                   TrainSystemInformation trainSystemInformation) {
        List<EndSegmentsEntity> endSegmentsInformation = new ArrayList<>();
        Transaction transaction = null;

        try (Session session = databaseInterface.createSession()) {
            transaction = session.beginTransaction();

            // Retrieve end segment information
            Query query = session.createQuery(DatabaseQueries.GET_END_SEGMENTS_FROM_TRAIN_SYSTEM);
            query.setParameter("train_system_name", trainSystemInformation.getName());

            List endSegmentsEntities = query.list();

            // Only two end segment information should be returned
            assert endSegmentsEntities.size() == 2 : "Not two end segment information have been returned";

            // There should only be one north end segment
            assert ((EndSegmentsEntity) endSegmentsEntities.get(0)).getIsNorthEnd()
                    != ((EndSegmentsEntity) endSegmentsEntities.get(1)).getIsNorthEnd() : "North end segments are" +
                    " incorrectly configured.";

            // Add each end segment into the list
            for (Object endSegmentEntity : endSegmentsEntities) {
                endSegmentsInformation.add((EndSegmentsEntity) endSegmentEntity);
            }

            transaction.commit();
        } catch (HibernateException exception) {
            if (transaction != null) {
                transaction.rollback();
            }

            exception.printStackTrace();
        }

        return endSegmentsInformation;
    }

    // Retrieve all trains of a given train system
    public static List<Train> getTrains(DatabaseInterface databaseInterface, TrainSystem trainSystem) {
        List<Train> trains = new ArrayList<>();
        Transaction transaction = null;

        try (Session session = databaseInterface.createSession()) {
            transaction = session.beginTransaction();

            // Retrieve list of trains
            Query query = session.createQuery(DatabaseQueries.GET_TRAINS_FROM_TRAIN_SYSTEM);
            query.setParameter("train_system_name", trainSystem.getTrainSystemInformation().getName());

            List trainsEntities = query.list();

            // Add each train into the list
            for (Object trainsEntity : trainsEntities) {
                trains.add(new Train(trainSystem, (TrainsEntity) trainsEntity));
            }

            transaction.commit();
        } catch (HibernateException exception) {
            if (transaction != null) {
                transaction.rollback();
            }

            exception.printStackTrace();
        }

        return trains;
    }

    // Retrieve a set of train carriages of a given train
    public static List<TrainCarriagesEntity> getTrainCarriages(DatabaseInterface databaseInterface, Train parentTrain) {
        List<TrainCarriagesEntity> trainCarriages = new ArrayList<>();
        Transaction transaction = null;

        try (Session session = databaseInterface.createSession()) {
            transaction = session.beginTransaction();

            // Retrieve the number of inactive trains
            Query query = session.createQuery(DatabaseQueries.GET_TRAIN_CARRIAGES_FROM_TRAIN);
            query.setParameter("train_id", parentTrain.getIdentifier());

            List result = query.list();

            // Add each carriage into the list
            for (Object trainCarriagesEntity : result) {
                trainCarriages.add((TrainCarriagesEntity) trainCarriagesEntity);
            }

            transaction.commit();
        } catch (HibernateException exception) {
            if (transaction != null) {
                transaction.rollback();
            }

            exception.printStackTrace();
        }

        return trainCarriages;
    }

    // Retrieve all peak hour schedules of a given platform
    // TODO: Finish implementation
    public static List<Schedule> getPeakHourSchedules(DatabaseInterface databaseInterface, TrainSystemInformation trainSystemInformation,
                                                      final int platformId) {
        List<Schedule> peakHours = new ArrayList<>();
        Transaction transaction = null;

        try (Session session = databaseInterface.createSession()) {
            transaction = session.beginTransaction();

            // Retrieve list of peak hours
            Query query = session.createQuery(DatabaseQueries.GET_PEAK_HOURS_FROM_PLATFORM);
            query.setParameter("platform_id", platformId);

            List peakHoursEntities = query.list();

            // Add each peak hour into the list
            for (Object peakHoursEntity : peakHoursEntities) {
                //peakHours.add(new Schedule());
            }

            transaction.commit();
        } catch (HibernateException exception) {
            if (transaction != null) {
                transaction.rollback();
            }

            exception.printStackTrace();
        }

        return peakHours;
    }
}
