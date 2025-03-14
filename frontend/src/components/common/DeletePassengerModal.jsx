import { Box, Typography, Button, Modal, Fade } from '@mui/material';

const DeletePassengerModal = ({ open, onClose, onConfirm }) => {
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
            width: '350px',
            maxWidth: '90%',
            textAlign: 'center',
            background: 'linear-gradient(135deg, #ffffff 0%, #f5f7fa 100%)',
            boxShadow: '0 12px 32px rgba(0,0,0,0.2)',
          }}
        >
          <Typography variant="h6" sx={{ fontWeight: 700, color: '#1976d2', mb: 2 }}>
            Удалить пассажира?
          </Typography>

          <Box sx={{ display: 'flex', gap: 2, justifyContent: 'center' }}>
            <Button
              variant="contained"
              onClick={onConfirm}
              sx={{
                borderRadius: '20px',
                background: 'linear-gradient(90deg, #1976d2 0%, #42a5f5 100%)',
                fontWeight: 600,
                '&:hover': {
                  background: 'linear-gradient(90deg, #1565c0 0%, #2196f3 100%)',
                },
              }}
            >
              Да
            </Button>
            <Button
              variant="contained"
              onClick={onClose}
              sx={{
                borderRadius: '20px',
                background: 'linear-gradient(90deg, #d32f2f 0%, #f44336 100%)',
                fontWeight: 600,
                '&:hover': {
                  background: 'linear-gradient(90deg, #b71c1c 0%, #d32f2f 100%)',
                },
              }}
            >
              Нет
            </Button>
          </Box>
        </Box>
      </Fade>
    </Modal>
  );
};

export default DeletePassengerModal;