import { useState, useEffect } from 'react';
import { Box, Typography, Dialog, DialogTitle, DialogContent, Button, Paper, Divider, Grid, Select, MenuItem, FormControl, InputLabel } from '@mui/material';
import { DirectionsBus, Train, Flight } from '@mui/icons-material';
import api from '../../api/api';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

const SeatSelectionDialog = ({ open, onClose, route, segments, seatsData, departureCity, arrivalCity }) => {
  const [selectedSeats, setSelectedSeats] = useState({});
  const [selectedPassenger, setSelectedPassenger] = useState('');
  const [passengers, setPassengers] = useState([]);
  const [error, setError] = useState('');
  const { userId, isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const firstSegment = segments[0] || {};
  const lastSegment = segments[segments.length - 1] || {};

  useEffect(() => {
    if (open) {
      setError('');
    }
  }, [open]);

  useEffect(() => {
    const fetchPassengers = async () => {
      if (!userId || !isAuthenticated) {
        setPassengers([]);
        return;
      }

      try {
        const token = localStorage.getItem('token');
        if (!token) {
          setError('Токен отсутствует. Пожалуйста, войдите снова.');
          setPassengers([]);
          return;
        }

        const response = await api.get(`/users/${userId}/passengers`, {
          headers: { Authorization: `Bearer ${token}` },
        });
        if (response.status === 200) {
          setPassengers(response.data || []);
        }
      } catch (err) {
        console.error('Ошибка загрузки пассажиров:', err);
        setPassengers([]);
        setError('Не удалось загрузить список пассажиров.');
      }
    };

    fetchPassengers();
  }, [userId, isAuthenticated]);

  const handleSeatSelect = (trip, seatNumber) => {
    const segmentData = seatsData.find(data => data.ticketId.trip === trip);
    const seat = segmentData?.seats.find(s => s.seatNumber === seatNumber);
    if (seat?.isAvailable) {
      setSelectedSeats(prev => ({
        ...prev,
        [trip]: prev[trip] === seatNumber ? null : seatNumber,
      }));
    }
  };

  const getSeatColor = (seat, trip) => {
    if (!seat.isAvailable) return 'grey';
    if (selectedSeats[trip] === seat.seatNumber) return '#4CAF50';
    if (seat.ticketType === 'luxury') return '#D4A017';
    return '#1976d2';
  };

  const handleBuyTickets = async () => {
    try {
      if (!selectedPassenger) {
        setError('Пожалуйста, выберите пассажира');
        return;
      }

      const selectedSeatEntries = Object.entries(selectedSeats).filter(([_, seatNumber]) => seatNumber);
      if (selectedSeatEntries.length === 0) {
        setError('Пожалуйста, выберите хотя бы одно место');
        return;
      }

      const bookingRequests = selectedSeatEntries.map(([trip, seatNumber]) => {
        const segmentData = seatsData.find(data => data.ticketId.trip === parseInt(trip));
        console.log('Segment data for trip', trip, ':', segmentData);
        if (!segmentData || !segmentData.routeIds) {
          throw new Error(`No routeIds found for trip ${trip}`);
        }
        return {
          routeIds: segmentData.routeIds,
          seatNumber,
          passengerId: parseInt(selectedPassenger),
        };
      });
      console.log('Booking requests:', bookingRequests);

      const token = localStorage.getItem('token');
      if (!token) {
        setError('Токен отсутствует. Пожалуйста, войдите снова.');
        return;
      }

      console.log('Sending booking request to API...');
      const response = await api.post('/tickets/book-multiple', bookingRequests, {
        headers: { Authorization: `Bearer ${token}` },
      });
      console.log('Booking response:', response.data);

      if (response.status === 200) {
        alert('Билеты успешно забронированы!');
        setSelectedSeats({});
        setSelectedPassenger('');
        onClose();
        navigate('/user', { state: { tab: 2 } });
      }
    } catch (err) {
      console.error('Booking error:', err.response ? err.response.data : err.message);
      if (err.response && err.response.status === 409) {
        setError('Ой, похоже, этот билет уже купили');
      } else {
        setError('Ошибка бронирования: ' + (err.response?.data || err.message));
      }
    }
  };

  const renderSeat = (seat, trip) => (
    <Box
      key={`${trip}-seat-${seat.seatNumber}`}
      sx={{
        textAlign: 'center',
        mx: 0.5,
        my: 0.5,
      }}
    >
      <Box
        sx={{
          width: 40,
          height: 40,
          backgroundColor: getSeatColor(seat, trip),
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          cursor: seat.isAvailable ? 'pointer' : 'not-allowed',
          border: '2px solid rgba(255, 255, 255, 0.3)',
          borderRadius: 8,
          transition: 'all 0.3s ease',
          '&:hover': {
            opacity: seat.isAvailable ? 0.8 : 1,
            transform: seat.isAvailable ? 'scale(1.1)' : 'none',
            boxShadow: seat.isAvailable ? '0 4px 12px rgba(0,0,0,0.2)' : 'none',
          },
        }}
        onClick={() => handleSeatSelect(trip, seat.seatNumber)}
      >
        <Typography variant="body2" sx={{ color: 'white', fontWeight: 'bold' }}>
          {seat.seatNumber}
        </Typography>
      </Box>
    </Box>
  );

  const renderSeatLayout = (segment) => {
    const segmentData = seatsData.find(data => data.ticketId.trip === segment.trip);
    const segmentSeats = segmentData ? segmentData.seats : [];
    const transportType = (segmentData?.ticketId.transportType || segment.transportType || '').toUpperCase();

    let seatsPerColumn = 4;

    const allSeats = [...segmentSeats].sort((a, b) => a.seatNumber - b.seatNumber);
    const luxurySeats = allSeats.filter(s => s.ticketType === 'luxury');
    const economySeats = allSeats.filter(s => s.ticketType === 'economy');

    const groupIntoColumns = (seats, totalColumns) => {
      const columns = Array.from({ length: totalColumns }, () => []);
      seats.forEach((seat, index) => {
        const columnIndex = index % totalColumns;
        columns[columnIndex].push(seat);
      });
      return columns;
    };

    const luxuryColumns = groupIntoColumns(luxurySeats, seatsPerColumn);
    const economyColumns = groupIntoColumns(economySeats, seatsPerColumn);

    const luxuryPrice = luxurySeats.length > 0 ? luxurySeats[0].price : 0;
    const economyPrice = economySeats.length > 0 ? economySeats[0].price : 0;

    const getTransportIcon = (type) => {
      switch (type) {
        case 'BUS':
          return <DirectionsBus sx={{ fontSize: '24px', color: '#1976d2', verticalAlign: 'middle', mr: 1 }} />;
        case 'TRAIN':
          return <Train sx={{ fontSize: '24px', color: '#1976d2', verticalAlign: 'middle', mr: 1 }} />;
        case 'AIR':
          return <Flight sx={{ fontSize: '24px', color: '#1976d2', verticalAlign: 'middle', mr: 1 }} />;
        default:
          return null;
      }
    };

    return (
      <Paper
        sx={{
          p: 3,
          mb: 3,
          borderRadius: '16px',
          background: 'linear-gradient(135deg, #ffffff 0%, #f5f7fa 100%)',
          boxShadow: '0 8px 24px rgba(0,0,0,0.1)',
          transition: 'transform 0.3s ease',
          '&:hover': {
            transform: 'translateY(-2px)',
          },
        }}
        key={segment.id}
      >
        {segment.trip && (
          <Typography variant="caption" sx={{ color: '#757575', mb: 1, display: 'block', textAlign: 'center' }}>
            Рейс {segment.trip}
          </Typography>
        )}
        <Typography
          variant="h6"
          sx={{
            fontWeight: 700,
            color: '#1976d2',
            mb: 3,
            textAlign: 'center',
            letterSpacing: '1px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
          }}
        >
          {getTransportIcon(transportType)}
          {segment.departureCity} → {segment.arrivalCity}
        </Typography>

        {luxurySeats.length > 0 && (
          <Box sx={{ mb: 3 }}>
            <Typography variant="h6" sx={{ fontWeight: 600, color: '#D4A017', mb: 2, textAlign: 'center' }}>
              Люкс места
            </Typography>
            <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'flex-start' }}>
              {luxuryColumns.map((column, colIndex) => (
                <Box
                  key={`luxury-col-${colIndex}`}
                  sx={{
                    display: 'flex',
                    flexDirection: 'column',
                    alignItems: 'center',
                    mr: colIndex === 1 ? 6 : 1,
                  }}
                >
                  {column.map(seat => renderSeat(seat, segment.trip))}
                </Box>
              ))}
            </Box>
          </Box>
        )}

        {economySeats.length > 0 && (
          <Box sx={{ mb: 3 }}>
            <Typography variant="h6" sx={{ fontWeight: 600, color: '#1976d2', mb: 2, textAlign: 'center' }}>
              Эконом места
            </Typography>
            <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'flex-start' }}>
              {economyColumns.map((column, colIndex) => (
                <Box
                  key={`economy-col-${colIndex}`}
                  sx={{
                    display: 'flex',
                    flexDirection: 'column',
                    alignItems: 'center',
                    mr: colIndex === 1 ? 6 : 1,
                  }}
                >
                  {column.map(seat => renderSeat(seat, segment.trip))}
                </Box>
              ))}
            </Box>
          </Box>
        )}

        <Box sx={{ mt: 3, display: 'flex', gap: 3, justifyContent: 'center', flexWrap: 'wrap' }}>
          {luxurySeats.length > 0 && (
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <Box sx={{ width: 40, height: 40, backgroundColor: '#D4A017', border: '2px solid rgba(255, 255, 255, 0.3)', borderRadius: 8 }} />
              <Typography variant="body2" sx={{ color: '#757575' }}>Люкс ({luxuryPrice} руб)</Typography>
            </Box>
          )}
          {economySeats.length > 0 && (
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <Box sx={{ width: 40, height: 40, backgroundColor: '#1976d2', border: '2px solid rgba(255, 255, 255, 0.3)', borderRadius: 8 }} />
              <Typography variant="body2" sx={{ color: '#757575' }}>Эконом ({economyPrice} руб)</Typography>
            </Box>
          )}
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <Box sx={{ width: 40, height: 40, backgroundColor: '#4CAF50', border: '2px solid rgba(255, 255, 255, 0.3)', borderRadius: 8 }} />
            <Typography variant="body2" sx={{ color: '#757575' }}>Выбрано</Typography>
          </Box>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <Box sx={{ width: 40, height: 40, backgroundColor: 'grey', border: '2px solid rgba(255, 255, 255, 0.3)', borderRadius: 8 }} />
            <Typography variant="body2" sx={{ color: '#757575' }}>Забронировано</Typography>
          </Box>
        </Box>
      </Paper>
    );
  };

  const totalPrice = Object.entries(selectedSeats).reduce((sum, [trip, seatNumber]) => {
    if (!seatNumber) return sum;
    const segmentData = seatsData.find(data => data.ticketId.trip === parseInt(trip));
    const seat = segmentData?.seats.find(s => s.seatNumber === seatNumber);
    return sum + (seat?.price || 0);
  }, 0);

  const handleAddPassenger = () => {
    if (!isAuthenticated) {
      navigate('/login');
    } else {
      navigate('/user', { state: { tab: 1 } });
    }
  };

  return (
    <Dialog
      open={open}
      onClose={onClose}
      maxWidth="lg"
      fullWidth
      sx={{
        '& .MuiDialog-paper': {
          borderRadius: '20px',
          padding: 3,
          background: 'linear-gradient(135deg, #ffffff 0%, #f0f4f8 100%)',
          boxShadow: '0 12px 32px rgba(0,0,0,0.1)',
        },
      }}
    >
      <DialogTitle
        sx={{
          textAlign: 'center',
          fontSize: '28px',
          fontWeight: 700,
          color: '#1976d2',
          background: 'linear-gradient(90deg, #1976d2 0%, #42a5f5 100%)',
          borderRadius: '12px 12px 0 0',
          color: '#fff',
          mb: 3,
          py: 2,
        }}
      >
        {firstSegment.departureCity} → {lastSegment.arrivalCity}
      </DialogTitle>
      <DialogContent>
        <Box sx={{ display: 'flex', justifyContent: 'center', gap: 3 }}>
          <Grid item xs={8}>
            <Box
              sx={{
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                maxHeight: '70vh',
                overflowY: 'auto',
                '&::-webkit-scrollbar': {
                  width: '8px',
                },
                '&::-webkit-scrollbar-track': {
                  background: '#f0f4f8',
                  borderRadius: '4px',
                },
                '&::-webkit-scrollbar-thumb': {
                  background: '#1976d2',
                  borderRadius: '4px',
                },
                '&::-webkit-scrollbar-thumb:hover': {
                  background: '#42a5f5',
                },
              }}
            >
              {segments.map((segment) => (
                <Box
                  key={segment.id}
                  sx={{
                    mb: 3,
                    width: '100%',
                  }}
                >
                  {renderSeatLayout(segment)}
                </Box>
              ))}
            </Box>
          </Grid>
          <Grid item xs={4}>
            <Paper
              sx={{
                p: 3,
                height: '100%',
                borderRadius: '16px',
                background: 'linear-gradient(135deg, #ffffff 0%, #f5f7fa 100%)',
                boxShadow: '0 6px 18px rgba(0,0,0,0.1)',
                transition: 'transform 0.3s ease',
                '&:hover': {
                  transform: 'translateY(-2px)',
                },
              }}
            >
              <Typography
                variant="h6"
                sx={{
                  fontWeight: 700,
                  color: '#1976d2',
                  mb: 3,
                  textAlign: 'center',
                  textTransform: 'uppercase',
                  letterSpacing: '1px',
                }}
              >
                Итог заказа
              </Typography>
              <Typography
                variant="h6"
                sx={{ mt: 2, fontSize: '20px', color: '#1976d2', textAlign: 'center' }}
              >
                Итоговая стоимость: {totalPrice} руб
              </Typography>
              {error && (
                <Typography color="error" sx={{ mt: 2, textAlign: 'center' }}>
                  {error}
                </Typography>
              )}
              {passengers.length > 0 ? (
                <FormControl sx={{ mt: 3, mb: 2, minWidth: 250 }} size="small">
                  <InputLabel sx={{ color: '#757575' }}>Пассажир</InputLabel>
                  <Select
                    value={selectedPassenger}
                    onChange={(e) => setSelectedPassenger(e.target.value)}
                    label="Пассажир"
                    sx={{
                      '& .MuiSelect-select': { padding: '10px' },
                      '& .MuiOutlinedInput-root': {
                        borderRadius: '10px',
                        backgroundColor: '#fff',
                      },
                    }}
                  >
                    <MenuItem value="">
                      <em>Выберите пассажира</em>
                    </MenuItem>
                    {passengers.map((passenger) => (
                      <MenuItem
                        key={passenger.id}
                        value={passenger.id}
                        sx={{ fontSize: '16px' }}
                      >
                        {`${passenger.surname} ${passenger.name}`}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
              ) : (
                <Button
                  variant="contained"
                  color="primary"
                  sx={{
                    mt: 3,
                    mb: 2,
                    borderRadius: '20px',
                    padding: '10px 20px',
                    fontSize: '16px',
                    textTransform: 'none',
                    background: 'linear-gradient(90deg, #1976d2 0%, #42a5f5 100%)',
                    '&:hover': {
                      background: 'linear-gradient(90deg, #1565c0 0%, #2196f3 100%)',
                    },
                  }}
                  onClick={handleAddPassenger}
                >
                  Добавить пассажира
                </Button>
              )}
              <Button
                variant="contained"
                color="success"
                sx={{
                  mt: 2,
                  mb: 2,
                  borderRadius: '20px',
                  padding: '10px 20px',
                  fontSize: '16px',
                  textTransform: 'none',
                  background: 'linear-gradient(90deg, #4CAF50 0%, #66BB6A 100%)',
                  '&:hover': {
                    background: 'linear-gradient(90deg, #388E3C 0%, #43A047 100%)',
                  },
                  width: '100%',
                }}
                disabled={totalPrice === 0 || !selectedPassenger}
                onClick={handleBuyTickets}
              >
                Купить
              </Button>
              <Divider sx={{ my: 3, borderColor: '#e0e0e0' }} />
              <Box sx={{ mt: 3 }}>
                {Object.entries(selectedSeats).map(([trip, seatNumber]) => {
                  if (!seatNumber) return null;
                  const segmentData = seatsData.find(data => data.ticketId.trip === parseInt(trip));
                  const seat = segmentData?.seats.find(s => s.seatNumber === seatNumber);
                  const segment = segments.find(s => s.trip === parseInt(trip)) || firstSegment;
                  return (
                    <Box
                      key={trip}
                      sx={{
                        display: 'flex',
                        justifyContent: 'space-between',
                        alignItems: 'center',
                        mb: 2,
                        p: 2,
                        backgroundColor: '#f0f4f8',
                        borderRadius: '12px',
                        transition: 'all 0.3s ease',
                        '&:hover': { backgroundColor: '#e0e8f0', transform: 'translateX(4px)' },
                      }}
                    >
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                        {segment?.trip && (
                          <Typography variant="caption" sx={{ color: '#757575' }}>
                            Рейс {segment.trip}
                          </Typography>
                        )}
                        <Typography variant="body2" sx={{ fontWeight: 500, color: '#333' }}>
                          Место {seatNumber} ({segment?.departureCity} → {segment?.arrivalCity})
                        </Typography>
                      </Box>
                      <Typography variant="body2" sx={{ fontWeight: 500, color: '#1976d2' }}>
                        {seat?.price} руб
                      </Typography>
                    </Box>
                  );
                })}
              </Box>
            </Paper>
          </Grid>
        </Box>
      </DialogContent>
    </Dialog>
  );
};

export default SeatSelectionDialog;