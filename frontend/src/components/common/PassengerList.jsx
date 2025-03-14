import { useState, useEffect } from 'react';
import { Box, Button, Typography } from '@mui/material';
import api from '../../api/api';
import PassengerCard from './PassengerCard';
import AddPassengerModal from './AddPassengerModal';
import InfoPassengerModal from './InfoPassengerModal';
import DeletePassengerModal from './DeletePassengerModal';

const PassengerList = ({ userId, onPassengerAdded }) => {
  const [passengers, setPassengers] = useState([]);
  const [openAddModal, setOpenAddModal] = useState(false);
  const [openInfoModal, setOpenInfoModal] = useState(false);
  const [openDeleteModal, setOpenDeleteModal] = useState(false);
  const [selectedPassenger, setSelectedPassenger] = useState(null);
  const [error, setError] = useState('');

  const fetchPassengers = async () => {
    try {
      const response = await api.get(`/users/${userId}/passengers`);
      setPassengers(response.data);
    } catch (err) {
      setError('Ошибка загрузки пассажиров: ' + err.message);
    }
  };

  useEffect(() => {
    if (userId) {
      fetchPassengers();
    }
  }, [userId]);

  const handleOpenAddModal = () => setOpenAddModal(true);
  const handleCloseAddModal = () => setOpenAddModal(false);

  const handleSavePassenger = async (passengerData, validationError) => {
    if (validationError) {
      setError(validationError);
      return;
    }
    try {
      console.log('Отправляемые данные:', passengerData);
      const response = await api.post(`/users/${userId}/passengers`, passengerData);
      console.log('Ответ от сервера:', response.data);
      handleCloseAddModal();
      fetchPassengers();
      if (onPassengerAdded) onPassengerAdded();
      setError('');
    } catch (err) {
      console.error('Ошибка при добавлении пассажира:', err.response || err);
      setError('Ошибка добавления пассажира: ' + (err.response?.data?.message || err.message));
    }
  };

  const handleOpenInfoModal = async (passengerId) => {
    try {
      const response = await api.get(`/users/${userId}/passengers/${passengerId}`);
      setSelectedPassenger(response.data);
      setOpenInfoModal(true);
    } catch (err) {
      setError('Ошибка загрузки данных пассажира: ' + err.message);
    }
  };

  const handleCloseInfoModal = () => {
    setOpenInfoModal(false);
    setSelectedPassenger(null);
  };

  const handleOpenDeleteModal = (passengerId) => {
    setSelectedPassenger(passengers.find(p => p.id === passengerId));
    setOpenDeleteModal(true);
  };

  const handleCloseDeleteModal = () => {
    setOpenDeleteModal(false);
    setSelectedPassenger(null);
  };

  const handleDeletePassenger = async () => {
    try {
      await api.delete(`/users/${userId}/passengers/${selectedPassenger.id}`);
      handleCloseDeleteModal();
      fetchPassengers();
      if (onPassengerAdded) onPassengerAdded();
    } catch (err) {
      setError('Ошибка удаления пассажира: ' + err.message);
    }
  };

  return (
    <Box>
      <Button
        variant="contained"
        onClick={handleOpenAddModal}
        sx={{
          mb: 3,
          borderRadius: '20px',
          background: 'linear-gradient(90deg, #4CAF50 0%, #66BB6A 100%)',
          fontWeight: 600,
          '&:hover': {
            background: 'linear-gradient(90deg, #388E3C 0%, #43A047 100%)',
          },
        }}
      >
        Добавить пассажира
      </Button>

      {passengers.length === 0 ? (
        <Typography sx={{ color: '#fff', textAlign: 'center', fontSize: '1.2rem' }}>
          У вас пока нет добавленных пассажиров
        </Typography>
      ) : (
        passengers.map(passenger => (
          <PassengerCard
            key={passenger.id}
            passenger={passenger}
            onInfo={handleOpenInfoModal}
            onDelete={handleOpenDeleteModal}
          />
        ))
      )}

      <AddPassengerModal
        open={openAddModal}
        onClose={handleCloseAddModal}
        onSave={handleSavePassenger}
        error={error}
      />
      <InfoPassengerModal
        open={openInfoModal}
        onClose={handleCloseInfoModal}
        passenger={selectedPassenger}
      />
      <DeletePassengerModal
        open={openDeleteModal}
        onClose={handleCloseDeleteModal}
        onConfirm={handleDeletePassenger}
      />
      {error && (
        <Typography color="error" sx={{ mt: 2, textAlign: 'center', color: '#fff' }}>
          {error}
        </Typography>
      )}
    </Box>
  );
};

export default PassengerList;