import { Box, Button } from '@mui/material';
import { Flight, DirectionsBus, DirectionsRailway } from '@mui/icons-material';

const TransportFilters = ({ selectedTransport, onTransportChange }) => {
  return (
    <Box
      sx={{
        display: 'flex',
        gap: 2,
        justifyContent: 'center',
        flexWrap: 'wrap',
      }}
    >
      <Button
        variant={selectedTransport === 'mix' ? 'contained' : 'outlined'}
        color="primary"
        onClick={() => onTransportChange('mix')}
        sx={{
          fontSize: '18px',
          padding: '12px 20px',
          minWidth: '120px',
          minHeight: '60px',
          borderRadius: '8px',
          textTransform: 'none',
          backgroundColor: selectedTransport === 'mix' ? '#1976d2' : 'transparent',
          '&:hover': {
            backgroundColor: selectedTransport === 'mix' ? '#115293' : '#e3f2fd',
          },
          transition: 'all 0.3s ease',
        }}
      >
        mix
      </Button>
      <Button
        variant={selectedTransport === 'air' ? 'contained' : 'outlined'}
        color="primary"
        startIcon={<Flight />}
        onClick={() => onTransportChange('air')}
        sx={{
          fontSize: '18px',
          padding: '12px 20px',
          minWidth: '120px',
          minHeight: '60px',
          borderRadius: '8px',
          textTransform: 'none',
          backgroundColor: selectedTransport === 'air' ? '#1976d2' : 'transparent',
          '&:hover': {
            backgroundColor: selectedTransport === 'air' ? '#115293' : '#e3f2fd',
          },
          transition: 'all 0.3s ease',
        }}
      >
        air
      </Button>
      <Button
        variant={selectedTransport === 'train' ? 'contained' : 'outlined'}
        color="primary"
        startIcon={<DirectionsRailway />}
        onClick={() => onTransportChange('train')}
        sx={{
          fontSize: '18px',
          padding: '12px 20px',
          minWidth: '120px',
          minHeight: '60px',
          borderRadius: '8px',
          textTransform: 'none',
          backgroundColor: selectedTransport === 'train' ? '#1976d2' : 'transparent',
          '&:hover': {
            backgroundColor: selectedTransport === 'train' ? '#115293' : '#e3f2fd',
          },
          transition: 'all 0.3s ease',
        }}
      >
        train
      </Button>
      <Button
        variant={selectedTransport === 'bus' ? 'contained' : 'outlined'}
        color="primary"
        startIcon={<DirectionsBus />}
        onClick={() => onTransportChange('bus')}
        sx={{
          fontSize: '18px',
          padding: '12px 20px',
          minWidth: '120px',
          minHeight: '60px',
          borderRadius: '8px',
          textTransform: 'none',
          backgroundColor: selectedTransport === 'bus' ? '#1976d2' : 'transparent',
          '&:hover': {
            backgroundColor: selectedTransport === 'bus' ? '#115293' : '#e3f2fd',
          },
          transition: 'all 0.3s ease',
        }}
      >
        bus
      </Button>
    </Box>
  );
};

export default TransportFilters;