export const searchRoutes = async (params) => {
  try {
    const queryParams = new URLSearchParams({
      departureCity: params.departureCity,
      arrivalCity: params.arrivalCity,
      ...(params.startTime && { startTime: params.startTime }),
      ...(params.directOnly !== undefined && { directOnly: params.directOnly }),
      ...(params.transportType && { transportType: params.transportType }),
      ...(params.sort && { sort: params.sort }),
      ...(params.multiStop !== undefined && { multiStop: params.multiStop }),
    }).toString();

    const response = await fetch(`http://localhost:8080/api/routes/search?${queryParams}`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
    });

    if (!response.ok) {
      throw new Error(`Search API failed: ${response.statusText}`);
    }

    const data = await response.json();
    return data;
  } catch (err) {
    throw err;
  }
};

export const directByDateRoutes = async (params) => {
  try {
    const queryParams = new URLSearchParams({
      departureCity: params.departureCity,
      arrivalCity: params.arrivalCity,
      ...(params.transportType && { transportType: params.transportType }),
      ...(params.sort && { sort: params.sort }),
    }).toString();
    const response = await fetch(`http://localhost:8080/api/routes/direct-by-date?${queryParams}`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
    });

    if (!response.ok) {
      throw new Error(`Direct-by-date API failed: ${response.statusText}`);
    }

    const data = await response.json();
    return data;
  } catch (err) {
    throw err;
  }
};

export const getRouteById = async (routeId) => {
  try {
    const response = await fetch(`http://localhost:8080/api/routes/${routeId}`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${localStorage.getItem('token')}`,
      },
    });
    if (!response.ok) {
      throw new Error(`Route API failed: ${response.statusText}`);
    }
    const data = await response.json();
    return data;
  } catch (err) {
    throw err;
  }
};