import { useState, useEffect } from 'react';
import { useNavigate, useLocation, Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';
import { jwtDecode } from 'jwt-decode';
import { Box, Button, Tab, Tabs, Typography, FormControl, InputLabel, Select, MenuItem, FormControlLabel, Checkbox, IconButton, Dialog, DialogTitle, DialogContent, DialogActions, Fade, CircularProgress } from '@mui/material';
import { Person as PersonIcon, Refresh as RefreshIcon, Home as HomeIcon, AccountCircle as AccountCircleIcon, Logout as LogoutIcon } from '@mui/icons-material';
import PassengerList from '../components/common/PassengerList';
import api from '../api/api';
import TicketCard from '../components/common/TicketCard';
import { formatDate, formatTime, calculateDuration } from '../utils/dateUtils';

function User() {
  const { isAuthenticated, user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [tabValue, setTabValue] = useState(0);
  const [profile, setProfile] = useState(null);
  const [userId, setUserId] = useState(null);
  const [passengers, setPassengers] = useState([]);
  const [selectedPassenger, setSelectedPassenger] = useState('all');
  const [showHistory, setShowHistory] = useState(false);
  const [tickets, setTickets] = useState([]);
  const [loading, setLoading] = useState(false);
  const [openDialog, setOpenDialog] = useState(false);
  const [ticketToCancel, setTicketToCancel] = useState(null);

  useEffect(() => {
    if (isAuthenticated) {
      const token = localStorage.getItem('token');
      if (token) {
        try {
          const decoded = jwtDecode(token);
          setUserId(decoded.userId);
        } catch (err) {
          console.error('Error decoding token:', err);
        }
      }
      fetchProfile();
      if (userId) {
        fetchPassengers();
        fetchTickets();
      }
      if (location.state && typeof location.state.tab === 'number') {
        setTabValue(location.state.tab);
      } else {
        setTabValue(0);
      }
    }
  }, [isAuthenticated, location.state, userId]);

  const fetchProfile = async () => {
    try {
      const token = localStorage.getItem('token');
      if (!token) throw new Error('Токен не найден');
      const decoded = jwtDecode(token);
      const userId = decoded.userId;
      if (!userId) throw new Error('User ID not found in token');
      const response = await api.get(`/users/${userId}/profile`);
      if (response.status === 200) {
        setProfile(response.data);
      }
    } catch (err) {
      console.error(`Ошибка сервера: ${err.message}`);
    }
  };

  const fetchPassengers = async () => {
    try {
      const response = await api.get(`/users/${userId}/passengers`);
      if (response.status === 200) setPassengers(response.data);
    } catch (err) {
      console.error('Fetch passengers error:', err);
    }
  };

  const fetchTickets = async () => {
    if (!userId) return;
    setLoading(true);
    try {
      let url = `/users/${userId}/tickets`;
      const params = { showHistory };

      if (selectedPassenger !== 'all') {
        url = `/users/${userId}/passengers/${selectedPassenger}/tickets`;
      }

      const response = await api.get(url, { params });
      if (response.status === 200) setTickets(response.data);
    } catch (err) {
      console.error('Fetch tickets error:', err);
    } finally {
      setLoading(false);
    }
  };

  const handlePassengerAdded = () => {
    fetchPassengers();
    fetchTickets();
  };

  const handleTabChange = (event, newValue) => {
    if (isAuthenticated) {
      setTabValue(newValue);
    }
  };

  const handleCancelTicket = async (routeId, seatNumber) => {
    setTicketToCancel({ routeId, seatNumber });
    setOpenDialog(true);
  };

  const confirmCancelTicket = async () => {
    if (ticketToCancel) {
      try {
        await api.post(`/routes/${ticketToCancel.routeId}/seats/${ticketToCancel.seatNumber}/cancel`);
        fetchTickets();
      } catch (err) {
        console.error('Cancel ticket error:', err);
      }
    }
    setOpenDialog(false);
    setTicketToCancel(null);
  };

  const cancelDialog = () => {
    setOpenDialog(false);
    setTicketToCancel(null);
  };

  if (!isAuthenticated) {
    return <Navigate to="/auth" replace />;
  }

  return (
    <Box
      sx={{
        minHeight: '100vh',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        backgroundColor: '#fff',
        padding: 3,
        width: '100vw',
      }}
    >
      <Box sx={{ width: '100%', maxWidth: '1200px' }}>
        <Fade in={true} timeout={1000}>
          <Box sx={{ textAlign: 'center', mb: 4 }}>
            <Typography
              variant="h3"
              sx={{
                fontWeight: 700,
                color: '#1976d2',
                letterSpacing: '2px',
              }}
            >
              JourneyHub
            </Typography>
          </Box>
        </Fade>

        <Box sx={{ display: 'flex', justifyContent: 'center', mb: 4 }}>
          <Button
            variant="contained"
            startIcon={<HomeIcon />}
            onClick={() => navigate('/')}
            sx={{
              borderRadius: '20px',
              backgroundColor: '#e0e0e0',
              color: '#1976d2',
              fontWeight: 600,
              mr: 2,
              '&:hover': {
                backgroundColor: '#d3d3d3',
              },
            }}
          >
            На главную
          </Button>
          <Button
            variant="outlined"
            startIcon={<LogoutIcon />}
            onClick={() => {
              logout();
              navigate('/auth');
            }}
            sx={{
              borderRadius: '20px',
              borderColor: '#1976d2',
              color: '#1976d2',
              fontWeight: 600,
              '&:hover': {
                borderColor: '#42a5f5',
                backgroundColor: 'rgba(25, 118, 210, 0.1)',
              },
            }}
          >
            Выход
          </Button>
        </Box>

        <Tabs
          value={tabValue}
          onChange={handleTabChange}
          centered
          sx={{
            mb: 4,
            '& .MuiTab-root': {
              fontSize: '1.1rem',
              fontWeight: 600,
              color: '#757575',
              textTransform: 'none',
            },
            '& .Mui-selected': {
              color: '#1976d2',
            },
            '& .MuiTabs-indicator': {
              backgroundColor: '#1976d2',
            },
          }}
        >
          <Tab label="Аккаунт" disabled={!isAuthenticated} />
          <Tab label="Мои пассажиры" disabled={!isAuthenticated} />
          <Tab label="Мои билеты" disabled={!isAuthenticated} />
        </Tabs>

        <Fade in={true} timeout={1500}>
          <Box sx={{ mt: 4, maxWidth: '800px', mx: 'auto', width: '100%', textAlign: 'left' }}>
            {tabValue === 0 && (
              <Box
                sx={{
                  p: 3,
                  borderRadius: '16px',
                  backgroundColor: '#fff',
                  boxShadow: '0 4px 12px rgba(0,0,0,0.05)',
                  transition: 'transform 0.3s ease',
                  '&:hover': {
                    transform: 'translateY(-2px)',
                  },
                }}
              >
                <Typography variant="h5" sx={{ fontWeight: 700, color: '#1976d2', mb: 2 }}>
                  Информация об аккаунте
                </Typography>
                <Box sx={{ mb: 2 }}>
                  <Typography variant="body1" sx={{ fontWeight: 500, color: '#333' }}>
                    Имя: <Typography component="span" sx={{ color: '#757575' }}>{profile?.name || 'Не указано'}</Typography>
                  </Typography>
                </Box>
                <Box sx={{ mb: 2 }}>
                  <Typography variant="body1" sx={{ fontWeight: 500, color: '#333' }}>
                    Фамилия: <Typography component="span" sx={{ color: '#757575' }}>{profile?.surname || 'Не указано'}</Typography>
                  </Typography>
                </Box>
                <Box sx={{ mb: 2 }}>
                  <Typography variant="body1" sx={{ fontWeight: 500, color: '#333' }}>
                    Email: <Typography component="span" sx={{ color: '#757575' }}>{profile?.email || 'Не указано'}</Typography>
                  </Typography>
                </Box>
              </Box>
            )}
            {tabValue === 1 && <PassengerList userId={userId} onPassengerAdded={handlePassengerAdded} />}
            {tabValue === 2 && (
              <Box>
                <Box sx={{ display: 'flex', alignItems: 'center', mb: 3, flexWrap: 'wrap', gap: 2 }}>
                  <FormControl sx={{ minWidth: 200 }}>
                    <InputLabel sx={{ color: '#757575' }}>Пассажир</InputLabel>
                    <Select
                      value={selectedPassenger}
                      onChange={(e) => setSelectedPassenger(e.target.value)}
                      label="Пассажир"
                      sx={{
                        borderRadius: '10px',
                        backgroundColor: '#fff',
                        '& .MuiOutlinedInput-root': {
                          '&:hover fieldset': {
                            borderColor: '#1976d2',
                          },
                          '&.Mui-focused fieldset': {
                            borderColor: '#1976d2',
                          },
                        },
                      }}
                    >
                      <MenuItem value="all">Все</MenuItem>
                      {passengers.map((passenger) => (
                        <MenuItem key={passenger.id} value={passenger.id}>
                          {passenger.name} {passenger.surname}
                        </MenuItem>
                      ))}
                    </Select>
                  </FormControl>

                  <FormControlLabel
                    control={
                      <Checkbox
                        checked={showHistory}
                        onChange={(e) => setShowHistory(e.target.checked)}
                        sx={{
                          color: '#757575',
                          '&.Mui-checked': {
                            color: '#1976d2',
                          },
                        }}
                      />
                    }
                    label="Показать прошлые билеты"
                    sx={{ color: '#757575' }}
                  />

                  <IconButton onClick={fetchTickets} disabled={loading} sx={{ color: '#757575' }}>
                    {loading ? <CircularProgress size={24}  sx={{ color: '#757575', mr: 100 }} /> : <RefreshIcon />}
                  </IconButton>
                </Box>

                {tickets.length === 0 ? (
                  <Typography sx={{ color: '#757575', textAlign: 'center', fontSize: '1.2rem' }}>
                    У вас пока нет билетов
                  </Typography>
                ) : (
                  tickets.map((ticket) => (
                    <TicketCard
                      key={`${ticket.routeId}-${ticket.seatNumber}`}
                      ticket={ticket}
                      onCancel={handleCancelTicket}
                    />
                  ))
                )}
              </Box>
            )}
          </Box>
        </Fade>

        <Dialog
          open={openDialog}
          onClose={cancelDialog}
          sx={{
            '& .MuiDialog-paper': {
              borderRadius: '20px',
              backgroundColor: '#fff',
              boxShadow: '0 4px 12px rgba(0,0,0,0.1)',
            },
          }}
        >
          <DialogTitle sx={{ fontWeight: 700, color: '#1976d2', textAlign: 'center' }}>
            Вернуть билет
          </DialogTitle>
          <DialogContent>
            <Typography sx={{ color: '#333', textAlign: 'center' }}>
              Вы уверены, что хотите вернуть билет?
            </Typography>
          </DialogContent>
          <DialogActions sx={{ justifyContent: 'center', pb: 3 }}>
            <Button
              onClick={cancelDialog}
              sx={{
                borderRadius: '20px',
                color: '#1976d2',
                borderColor: '#1976d2',
                '&:hover': {
                  backgroundColor: 'rgba(25, 118, 210, 0.1)',
                },
              }}
            >
              Нет
            </Button>
            <Button
              onClick={confirmCancelTicket}
              sx={{
                borderRadius: '20px',
                backgroundColor: '#d32f2f',
                color: '#fff',
                '&:hover': {
                  backgroundColor: '#b71c1c',
                },
              }}
            >
              Да
            </Button>
          </DialogActions>
        </Dialog>
      </Box>
    </Box>
  );
}

export default User;