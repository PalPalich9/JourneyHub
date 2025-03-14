import { Flight, DirectionsBus, DirectionsRailway } from '@mui/icons-material';

const TransportIcon = ({ transportType }) => {
  switch (transportType) {
    case 'air':
      return <Flight sx={{ fontSize: 40 }} />;
    case 'bus':
      return <DirectionsBus sx={{ fontSize: 40 }} />;
    case 'train':
      return <DirectionsRailway sx={{ fontSize: 40 }} />;
    default:
      return null;
  }
};

export default TransportIcon;