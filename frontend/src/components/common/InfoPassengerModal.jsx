import { Box, Typography, Button, Modal, Fade } from '@mui/material';

const InfoPassengerModal = ({ open, onClose, passenger }) => {
  const formatBirthDate = (birthDate) => {
    const date = new Date(birthDate);
    const day = String(date.getDate()).padStart(2, '0');
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const year = date.getFullYear();
    return `${day}.${month}.${year}`;
  };

  const isBirthCertificate = passenger?.passportNumber?.length === 6;
  const passportNumber = isBirthCertificate ? passenger?.passportNumber : passenger?.passportNumber?.slice(4);

  return (
    <Modal open={open} onClose={onClose}>
      <Fade in={open} timeout={500}>
        <Box
          sx={{
            position: 'absolute',
            top: '50%',
            left: '50%',
            transform: 'translate(-50%, -50%)',
            bgcolor: 'background.paper',
            borderRadius: '16px',
            p: 4,
            width: '450px',
            maxWidth: '90%',
            background: 'linear-gradient(135deg, #ffffff 0%, #f5f7fa 100%)',
            boxShadow: '0 12px 32px rgba(0,0,0,0.2)',
          }}
        >
          <Typography variant="h6" sx={{ fontWeight: 700, color: '#1976d2', mb: 3, textAlign: 'center' }}>
            Информация о пассажире
          </Typography>
          <Box sx={{ mb: 2 }}>
            <Typography component="span" sx={{ fontWeight: 500, color: '#333' }}>
              Фамилия: <Typography component="span" sx={{ color: '#757575', ml: 1 }}>{passenger?.surname}</Typography>
            </Typography>
          </Box>
          <Box sx={{ mb: 2 }}>
            <Typography component="span" sx={{ fontWeight: 500, color: '#333' }}>
              Имя: <Typography component="span" sx={{ color: '#757575', ml: 1 }}>{passenger?.name}</Typography>
            </Typography>
          </Box>
          {passenger?.patronymic && (
            <Box sx={{ mb: 2 }}>
              <Typography component="span" sx={{ fontWeight: 500, color: '#333' }}>
                Отчество: <Typography component="span" sx={{ color: '#757575', ml: 1 }}>{passenger.patronymic}</Typography>
              </Typography>
            </Box>
          )}
          {!isBirthCertificate && (
            <Box sx={{ mb: 2 }}>
              <Typography component="span" sx={{ fontWeight: 500, color: '#333' }}>
                Серия: <Typography component="span" sx={{ color: '#757575', ml: 1 }}>{passenger?.passportNumber?.slice(0, 4) || 'Не указана'}</Typography>
              </Typography>
            </Box>
          )}
          <Box sx={{ mb: 2 }}>
            <Typography component="span" sx={{ fontWeight: 500, color: '#333' }}>
              Номер: <Typography component="span" sx={{ color: '#757575', ml: 1 }}>{passportNumber || 'Не указан'}</Typography>
            </Typography>
          </Box>
          <Box sx={{ mb: 2 }}>
            <Typography component="span" sx={{ fontWeight: 500, color: '#333' }}>
              Пол: <Typography component="span" sx={{ color: '#757575', ml: 1 }}>
                {passenger?.gender === 'M' ? 'Мужской' : passenger?.gender === 'F' ? 'Женский' : 'Не указан'}
              </Typography>
            </Typography>
          </Box>
          <Box sx={{ mb: 2 }}>
            <Typography component="span" sx={{ fontWeight: 500, color: '#333' }}>
              Дата рождения: <Typography component="span" sx={{ color: '#757575', ml: 1 }}>
                {passenger?.birthDate ? formatBirthDate(passenger.birthDate) : 'Не указана'}
              </Typography>
            </Typography>
          </Box>
          <Button
            variant="contained"
            onClick={onClose}
            sx={{
              borderRadius: '20px',
              background: 'linear-gradient(90deg, #d32f2f 0%, #f44336 100%)',
              fontWeight: 600,
              mt: 2,
              width: '100%',
              '&:hover': {
                background: 'linear-gradient(90deg, #b71c1c 0%, #d32f2f 100%)',
              },
            }}
          >
            Закрыть
          </Button>
        </Box>
      </Fade>
    </Modal>
  );
};

export default InfoPassengerModal;