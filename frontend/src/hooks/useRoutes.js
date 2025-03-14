import { useState } from 'react';
import { searchRoutes, directByDateRoutes } from '../api/routeApi';

export const useRoutes = () => {
  const [routes, setRoutes] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchRoutes = async (departureCity, arrivalCity, departureTime, withTransfers, transportType, sortCriteria, multiStop = false) => {
    try {
      setLoading(true);
      setError(null);
      setRoutes(null);

      console.log('Fetching routes with params:', { departureCity, arrivalCity, departureTime, withTransfers, transportType, sortCriteria, multiStop });

      if (!departureCity || !arrivalCity) {
        console.log('No cities provided, aborting fetch');
        setRoutes(null);
        setLoading(false);
        return;
      }

      let result;
      if (departureTime) {
        const params = {
          departureCity,
          arrivalCity,
          startTime: departureTime,
          directOnly: !withTransfers,
          ...(transportType && { transportType }),
          sort: sortCriteria,
          multiStop,
        };
        console.log('Calling searchRoutes API with params:', params);
        const data = await searchRoutes(params);
        console.log('Search API response:', data);
        result = { type: 'search', data, departureTime };
      } else {
        const params = {
          departureCity,
          arrivalCity,
          ...(transportType && { transportType }),
          sort: sortCriteria,
        };
        console.log('Calling directByDateRoutes API with params:', params);
        const data = await directByDateRoutes(params);
        console.log('Direct-by-date API response:', data);
        result = { type: 'direct-by-date', data };
      }

      setRoutes(result);
    } catch (err) {
      console.error('API error:', err);
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return { routes, loading, error, fetchRoutes };
};