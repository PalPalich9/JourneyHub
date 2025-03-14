import { Box, Button } from '@mui/material';
import { Person, AirplaneTicket, Logout as LogoutIcon } from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext.jsx';

const ProfileActions = () => {
  const navigate = useNavigate();
  const { isAuthenticated, logout } = useAuth();

  return (
    <Box sx={{ display: 'flex', gap: 2, justifyContent: 'center', flexWrap: 'wrap' }}>
      <Button
        variant="outlined"
        startIcon={<Person />}
        onClick={() => {
          if (isAuthenticated) {
            navigate('/user', { state: { tab: 0 } });
          } else {
            navigate('/login');
          }
        }}
        disabled={!isAuthenticated}
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
        Профиль
      </Button>
      <Button
        variant="outlined"
        startIcon={<Person />}
        onClick={() => navigate('/user', { state: { tab: 1 } })}
        disabled={!isAuthenticated}
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
        Мои пассажиры
      </Button>
      <Button
        variant="outlined"
        startIcon={<AirplaneTicket />}
        onClick={() => navigate('/user', { state: { tab: 2 } })}
        disabled={!isAuthenticated}
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
        Мои билеты
      </Button>
      {isAuthenticated && (
        <Button
          variant="outlined"
          startIcon={<LogoutIcon />}
          onClick={() => {
            logout();
            navigate('/login');
          }}
          sx={{
            borderRadius: '20px',
            borderColor: '#d32f2f',
            color: '#d32f2f',
            fontWeight: 600,
            '&:hover': {
              borderColor: '#f44336',
              backgroundColor: 'rgba(211, 47, 47, 0.1)',
            },
          }}
        >
          Выход
        </Button>
      )}
    </Box>
  );
};

export default ProfileActions;