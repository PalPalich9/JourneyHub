import { useState } from 'react';
import { Box, Typography, Button, Fade } from '@mui/material';
import TransportIcon from './TransportIcon';
import { formatDate, formatTime, calculateDuration, formatTicketDuration } from '../../utils/dateUtilsTickets';
import api from '../../api/api';

const TicketCard = ({ ticket }) => {
  const [expanded, setExpanded] = useState(false);
  const departureTime = ticket.departureTime;
  const arrivalTime = ticket.arrivalTime;
  const travelDuration = ticket.travelDuration;
  const formattedDuration = formatTicketDuration(calculateDuration(departureTime, arrivalTime, travelDuration));

  const currentTime = new Date();
  const departureDateTime = new Date(departureTime);
  const timeDifferenceMinutes = (departureDateTime - currentTime) / (1000 * 60);
  const canCancel = timeDifferenceMinutes > 0; // Можно отменить, если отправление ещё не прошло

  const handleCancelTicket = async () => {
    try {
      const token = localStorage.getItem('token');
      console.log('Ticket data:', ticket);
      const cancelRequest = {
        routeIds: ticket.routeIds || [ticket.routeId], // Используем routeIds из ticket
        seatNumber: ticket.seatNumber,
        passengerId: ticket.passengerId,
      };
      console.log('Cancel request:', cancelRequest);
      const response = await api.post('/tickets/cancel', cancelRequest, {
        headers: { Authorization: `Bearer ${token}` },
      });
      if (response.status === 200) {
        alert('Билет успешно отменён!');
        window.location.reload();
      }
    } catch (err) {
      console.error('Cancel ticket error:', err);
      alert('Ошибка отмены билета: ' + (err.response?.data || err.message));
    }
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
        }}
      >
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <Box sx={{ display: 'flex', alignItems: 'center' }}>
            <TransportIcon transportType={ticket.transportType} sx={{ color: '#1976d2', fontSize: 40 }} />
            <Box sx={{ ml: 3 }}>
              {ticket.trip && (
                <Typography variant="caption" sx={{ color: '#757575', mb: 1, display: 'block' }}>
                  Рейс {ticket.trip}
                </Typography>
              )}
              <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                <Typography variant="body1" sx={{ fontWeight: 700, color: '#1976d2' }}>
                  {ticket.departureCity}
                </Typography>
                <Typography sx={{ mx: 1, color: '#757575' }}>→</Typography>
                <Typography variant="body1" sx={{ fontWeight: 700, color: '#1976d2' }}>
                  {ticket.arrivalCity}
                </Typography>
              </Box>
              <Box sx={{ display: 'flex', alignItems: 'center' }}>
                <Box sx={{ textAlign: 'center' }}>
                  <Typography variant="body1" sx={{ fontWeight: 500, color: '#333' }}>
                    {formatTime(departureTime)}
                  </Typography>
                  <Typography variant="caption" sx={{ color: '#757575' }}>
                    {formatDate(departureTime, 'dd MMMM')}
                  </Typography>
                </Box>
                <Box sx={{ mx: 3, textAlign: 'center' }}>
                  <Typography variant="caption" sx={{ color: '#757575' }}>
                    {formattedDuration}
                  </Typography>
                  <Box sx={{ borderBottom: '2px dashed #e0e0e0', width: '120px', mt: 1 }} />
                </Box>
                <Box sx={{ textAlign: 'center' }}>
                  <Typography variant="body1" sx={{ fontWeight: 500, color: '#333' }}>
                    {formatTime(arrivalTime)}
                  </Typography>
                  <Typography variant="caption" sx={{ color: '#757575' }}>
                    {formatDate(arrivalTime, 'dd MMMM')}
                  </Typography>
                </Box>
              </Box>
            </Box>
          </Box>
          <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end' }}>
            <Box sx={{ textAlign: 'right', mb: 2 }}>
              <Typography sx={{ fontWeight: 500, color: '#333' }}>
                Место: <Typography component="span" sx={{ color: '#1976d2' }}>{ticket.seatNumber}</Typography>
              </Typography>
              <Typography sx={{ fontWeight: 500, color: '#333' }}>
                Класс: <Typography component="span" sx={{ color: ticket.ticketType === 'luxury' ? '#D4A017' : '#1976d2' }}>
                  {ticket.ticketType === 'luxury' ? 'Люкс' : 'Эконом'}
                </Typography>
              </Typography>
              <Typography sx={{ fontWeight: 500, color: '#333' }}>
                Цена: <Typography component="span" sx={{ color: '#1976d2' }}>{ticket.price} руб</Typography>
              </Typography>
            </Box>
            <Button
              variant="contained"
              onClick={handleCancelTicket}
              disabled={!canCancel}
              sx={{
                borderRadius: '20px',
                background: canCancel
                  ? 'linear-gradient(90deg, #d32f2f 0%, #f44336 100%)'
                  : 'linear-gradient(90deg, #bdbdbd 0%, #e0e0e0 100%)',
                fontWeight: 600,
                '&:hover': {
                  background: canCancel
                    ? 'linear-gradient(90deg, #b71c1c 0%, #d32f2f 100%)'
                    : 'linear-gradient(90deg, #bdbdbd 0%, #e0e0e0 100%)',
                },
              }}
            >
              Вернуть билет
            </Button>
          </Box>
        </Box>
      </Box>
    </Fade>
  );
};

export default TicketCard;