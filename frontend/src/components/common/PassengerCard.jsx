import { Box, Typography, IconButton, Fade } from '@mui/material';
import { Delete as DeleteIcon, Info as InfoIcon } from '@mui/icons-material';

const PassengerCard = ({ passenger, onInfo, onDelete }) => {
  return (
    <Fade in={true} timeout={500}>
      <Box
        sx={{
          display: 'flex',
          alignItems: 'center',
          borderRadius: '12px',
          p: 2,
          mb: 2,
          background: 'linear-gradient(135deg, #ffffff 0%, #f5f7fa 100%)',
          boxShadow: '0 4px 12px rgba(0,0,0,0.1)',
          transition: 'transform 0.3s ease',
          '&:hover': {
            transform: 'translateY(-2px)',
            boxShadow: '0 6px 18px rgba(0,0,0,0.15)',
          },
        }}
      >
        <IconButton
          onClick={() => onInfo(passenger.id)}
          sx={{
            color: '#1976d2',
            '&:hover': {
              color: '#42a5f5',
            },
          }}
        >
          <InfoIcon />
        </IconButton>
        <Typography sx={{ flexGrow: 1, fontWeight: 500, color: '#333' }}>
          {passenger.name} {passenger.surname}
        </Typography>
        <IconButton
          onClick={() => onDelete(passenger.id)}
          sx={{
            color: '#d32f2f',
            '&:hover': {
              color: '#f44336',
            },
          }}
        >
          <DeleteIcon />
        </IconButton>
      </Box>
    </Fade>
  );
};

export default PassengerCard;