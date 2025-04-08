import { useState } from 'react';
import { Box, Button, Typography, IconButton } from '@mui/material';
import { ExpandMore } from '@mui/icons-material';
import TransportIcon from './TransportIcon';
import { formatDate, formatTime, calculateDuration } from '../../utils/dateUtils';
import SeatSelectionDialog from './SeatSelectionDialog';
import api from '../../api/api';
import Fade from '@mui/material/Fade';

const hasAvailableTicketsForRoute = (route) => {
  if (Array.isArray(route)) {
    return route.every(segment => segment?.hasAvailableTickets);
  }
  return route?.hasAvailableTickets || false;
};

const calculateTotalPrice = (route) => {
  if (Array.isArray(route)) {
    return route.reduce((total, segment) => total + (segment?.minPrice || 0), 0);
  }
  return route?.minPrice || 0;
};

const RouteCard = ({ route }) => {
  const [expanded, setExpanded] = useState(false);
  const [openDialog, setOpenDialog] = useState(false);
  const [seatsData, setSeatsData] = useState([]);

  if (!route) {
    return null;
  }

  const isTransferRoute = Array.isArray(route);
  const segments = isTransferRoute ? route : [route];

  const firstSegment = segments[0] || {};
  const lastSegment = segments[segments.length - 1] || {};

  const departureCity = firstSegment.departureCity || 'Не указано';
  const arrivalCity = lastSegment.arrivalCity || 'Не указано';
  const departureTime = firstSegment.departureTime;
  const arrivalTime = lastSegment.arrivalTime;
  const totalPrice = calculateTotalPrice(route);
  const hasAvailableTickets = hasAvailableTicketsForRoute(route);
  const totalDuration = departureTime && arrivalTime ? calculateDuration(departureTime, arrivalTime) : 'Не указано';

  const calculateWaitingTime = (arrivalTime, nextDepartureTime) => {
    if (!arrivalTime || !nextDepartureTime) return 'Не указано';
    const arrival = new Date(arrivalTime);
    const departure = new Date(nextDepartureTime);
    const diffMs = departure - arrival;
    const diffHours = Math.floor(diffMs / (1000 * 60 * 60));
    const diffMinutes = Math.floor((diffMs % (1000 * 60 * 60)) / (1000 * 60));
    return `${diffHours}ч ${diffMinutes}мин`;
  };

  const fetchSeats = async () => {
    try {
      const token = localStorage.getItem('token');
      const routeIdsGroups = segments.map(segment => segment.routeIds || [segment.id]).filter(Boolean);
      if (routeIdsGroups.length === 0) {
        console.error('No route IDs to fetch seats for');
        return;
      }
      const routeIdsParams = routeIdsGroups.map(group => group.join(',')).join('&routeIds=');
      const url = `/routes/seats?routeIds=${routeIdsParams}`;
      console.log('Fetching seats with URL:', url);
      const response = await api.get(url, {
        headers: { Authorization: `Bearer ${token}` },
      });
      console.log('Seats API response:', response.data);

      const transformedData = response.data.map((item, index) => {
        const segment = segments[index] || {};
        return {
          ticketId: {
            trip: item.ticketId.trip,
            transportType: item.ticketId.transportType || segment.transportType,
          },
          seats: item.seats || [],
          routeIds: item.routeIds || segment.routeIds || [segment.id], // Добавляем routeIds
        };
      });
      console.log('Transformed seats data:', transformedData);
      setSeatsData(transformedData);
    } catch (err) {
      console.error('Error fetching seats:', err.response ? err.response.data : err.message);
    }
  };

  const handleSelectSeat = async () => {
    await fetchSeats();
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
  };

  return (
    <Fade in={true} timeout={500}>
      <Box
        sx={{
          borderRadius: '16px',
          p: 3,
          mb: 3,
          background: 'linear-gradient(135deg, #ffffff 0%, #f5f7fa 100%)',
          boxShadow: '0 8px 24px rgba(0,0,0,0.1)',
          transition: 'transform 0.3s ease',
          '&:hover': {
            transform: 'translateY(-2px)',
            boxShadow: '0 12px 32px rgba(0,0,0,0.15)',
          },
          width: '100%',
          maxWidth: '800px',
        }}
      >
        {expanded ? (
          <Box>
            {segments.map((segment, index) => (
              <Box key={segment?.id || index} sx={{ mb: index < segments.length - 1 ? 3 : 0 }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <Box sx={{ display: 'flex', alignItems: 'center' }}>
                    <TransportIcon transportType={segment?.transportType} sx={{ fontSize: '40px', color: '#1976d2' }} />
                    <Box sx={{ ml: 3 }}>
                      {segment?.trip && isTransferRoute && (
                        <Typography variant="caption" sx={{ color: 'gray', mb: 1, display: 'block', fontSize: '16px' }}>
                          Рейс {segment.trip}
                        </Typography>
                      )}
                      <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                        <Typography variant="h6" sx={{ fontWeight: 'bold', fontSize: '22px', color: '#333' }}>
                          {segment?.departureCity || 'Не указано'}
                        </Typography>
                        <Typography sx={{ mx: 1, fontSize: '18px', color: '#666' }}>→</Typography>
                        <Typography variant="h6" sx={{ fontWeight: 'bold', fontSize: '22px', color: '#333' }}>
                          {segment?.arrivalCity || 'Не указано'}
                        </Typography>
                      </Box>
                      <Box sx={{ display: 'flex', alignItems: 'center' }}>
                        <Box sx={{ textAlign: 'center' }}>
                          <Typography variant="body1" sx={{ fontSize: '18px', color: '#333' }}>
                            {formatTime(segment?.departureTime) || 'Не указано'}
                          </Typography>
                          <Typography variant="caption" sx={{ fontSize: '14px', color: '#757575' }}>
                            {formatDate(segment?.departureTime, 'dd MMMM') || 'Не указано'}
                          </Typography>
                        </Box>
                        <Box sx={{ mx: 3, textAlign: 'center' }}>
                          <Typography variant="caption" sx={{ color: '#757575' }}>
                            {calculateDuration(segment?.departureTime, segment?.arrivalTime) || 'Не указано'}
                          </Typography>
                          <Box sx={{ borderBottom: '2px dashed #e0e0e0', width: '120px', mt: 1 }} />
                        </Box>
                        <Box sx={{ textAlign: 'center' }}>
                          <Typography variant="body1" sx={{ fontSize: '18px', color: '#333' }}>
                            {formatTime(segment?.arrivalTime) || 'Не указано'}
                          </Typography>
                          <Typography variant="caption" sx={{ fontSize: '14px', color: '#757575' }}>
                            {formatDate(segment?.arrivalTime, 'dd MMMM') || 'Не указано'}
                          </Typography>
                        </Box>
                      </Box>
                    </Box>
                  </Box>
                  <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end' }}>
                    {index === 0 && (
                      <>
                        <Button
                          variant="contained"
                          color="primary"
                          onClick={handleSelectSeat}
                          sx={{ mb: 1, borderRadius: '20px', padding: '8px 20px', fontSize: '16px', textTransform: 'none' }}
                          disabled={!segment?.hasAvailableTickets}
                        >
                          Выбрать место
                        </Button>
                        <IconButton onClick={() => setExpanded(false)}>
                          <ExpandMore sx={{ transform: 'rotate(180deg)', fontSize: '30px', color: '#1976d2' }} />
                        </IconButton>
                      </>
                    )}
                    <Box sx={{ display: 'flex', alignItems: 'center', mt: index === 0 ? 1 : 0 }}>
                      <Typography sx={{ color: segment?.hasAvailableTickets ? 'green' : 'red', fontSize: '16px', mr: 2 }}>
                        {segment?.hasAvailableTickets ? 'Есть билеты' : 'Билетов нет'}
                      </Typography>
                      <Typography variant="body1" sx={{ fontWeight: 'bold', fontSize: '20px', color: '#1976d2' }}>
                        от {segment?.minPrice || 0} руб
                      </Typography>
                    </Box>
                  </Box>
                </Box>
                {index < segments.length - 1 && (
                  <Box sx={{ mt: 2, mb: 2, p: 1, backgroundColor: '#f5f7fa', borderRadius: '6px', textAlign: 'center' }}>
                    <Typography variant="caption" sx={{ color: 'gray', fontSize: '14px' }}>
                      Пересадка через {calculateWaitingTime(segment?.arrivalTime, segments[index + 1]?.departureTime)}
                    </Typography>
                  </Box>
                )}
              </Box>
            ))}
          </Box>
        ) : (
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <Box sx={{ display: 'flex', alignItems: 'center', mr: 3 }}>
              {segments.map((segment, index) => (
                <TransportIcon key={index} transportType={segment?.transportType} sx={{ fontSize: '40px', color: '#1976d2' }} />
              ))}
            </Box>
            <Box sx={{ flexGrow: 1 }}>
              {!isTransferRoute && firstSegment?.trip && (
                <Typography variant="caption" sx={{ color: 'gray', mb: 1, display: 'block', fontSize: '16px' }}>
                  Рейс {firstSegment.trip}
                </Typography>
              )}
              <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                <Typography variant="body1" sx={{ fontWeight: 700, color: '#1976d2' }}>
                  {departureCity}
                </Typography>
                <Typography sx={{ mx: 1, color: '#757575' }}>→</Typography>
                <Typography variant="body1" sx={{ fontWeight: 700, color: '#1976d2' }}>
                  {arrivalCity}
                </Typography>
              </Box>
              <Box sx={{ display: 'flex', alignItems: 'center' }}>
                <Box sx={{ textAlign: 'center' }}>
                  <Typography variant="body1" sx={{ fontWeight: 500, color: '#333' }}>
                    {formatTime(departureTime) || 'Не указано'}
                  </Typography>
                  <Typography variant="caption" sx={{ color: '#757575' }}>
                    {formatDate(departureTime, 'dd MMMM') || 'Не указано'}
                  </Typography>
                </Box>
                <Box sx={{ mx: 3, textAlign: 'center' }}>
                  <Typography variant="caption" sx={{ color: '#757575' }}>
                    {totalDuration}
                  </Typography>
                  <Box sx={{ borderBottom: '2px dashed #e0e0e0', width: '120px', mt: 1 }} />
                </Box>
                <Box sx={{ textAlign: 'center' }}>
                  <Typography variant="body1" sx={{ fontWeight: 500, color: '#333' }}>
                    {formatTime(arrivalTime) || 'Не указано'}
                  </Typography>
                  <Typography variant="caption" sx={{ color: '#757575' }}>
                    {formatDate(arrivalTime, 'dd MMMM') || 'Не указано'}
                  </Typography>
                </Box>
              </Box>
            </Box>
            <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end' }}>
              <Button
                variant="contained"
                color="primary"
                onClick={handleSelectSeat}
                sx={{ mb: 1, borderRadius: '20px', padding: '8px 20px', fontSize: '16px', textTransform: 'none' }}
                disabled={!hasAvailableTickets}
              >
                Выбрать место
              </Button>
              {isTransferRoute && (
                <IconButton onClick={() => setExpanded(true)}>
                  <ExpandMore sx={{ fontSize: '30px', color: '#1976d2' }} />
                </IconButton>
              )}
              <Box sx={{ display: 'flex', alignItems: 'center', mt: 1 }}>
                <Typography sx={{ color: hasAvailableTickets ? 'green' : 'red', fontSize: '16px', mr: 2 }}>
                  {hasAvailableTickets ? 'Есть билеты' : 'Билетов нет'}
                </Typography>
                <Typography variant="body1" sx={{ fontWeight: 'bold', fontSize: '20px', color: '#1976d2' }}>
                  от {totalPrice} руб
                </Typography>
              </Box>
            </Box>
          </Box>
        )}
        <SeatSelectionDialog
          open={openDialog}
          onClose={handleCloseDialog}
          route={route}
          segments={segments}
          seatsData={seatsData}
          departureCity={departureCity}
          arrivalCity={arrivalCity}
        />
      </Box>
    </Fade>
  );
};

export default RouteCard;