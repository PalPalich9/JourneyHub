import { useState, useEffect } from 'react';
import { Box, Button, Typography, TextField, MenuItem, Select, FormHelperText, Modal, Fade } from '@mui/material';
import { Person as PersonIcon, CalendarToday as CalendarIcon, Badge as AssignmentIndIcon, Wc as GenderIcon } from '@mui/icons-material';

const AddPassengerModal = ({ open, onClose, onSave, error }) => {
  const [newPassenger, setNewPassenger] = useState({
    name: '',
    surname: '',
    patronymic: '',
    passportSeries: '',
    passportNumber: '',
    gender: '',
    birthDate: '',
  });

  const calculateAge = (birthDateStr) => {
    const [day, month, year] = birthDateStr.split('.').map(Number);
    const birthDate = new Date(year, month - 1, day);
    const today = new Date();
    let age = today.getFullYear() - birthDate.getFullYear();
    const monthDiff = today.getMonth() - birthDate.getMonth();
    const dayDiff = today.getDate() - birthDate.getDate();
    if (monthDiff < 0 || (monthDiff === 0 && dayDiff < 0)) {
      age--;
    }
    return age;
  };

  const isUnder14 = () => {
    if (!newPassenger.birthDate || newPassenger.birthDate.length !== 8) return false;
    const formattedDate = `${newPassenger.birthDate.slice(0, 2)}.${newPassenger.birthDate.slice(2, 4)}.${newPassenger.birthDate.slice(4)}`;
    return calculateAge(formattedDate) < 14;
  };

  const validateNameField = (value) => {
    const cyrillicRegex = /^[А-Я][а-я]*$/;
    return cyrillicRegex.test(value);
  };

  const handleNameFieldChange = (field, value) => {
    let filteredValue = value.replace(/[^а-яА-Я]/g, '');
    if (filteredValue.length > 0) {
      filteredValue = filteredValue.charAt(0).toUpperCase() + filteredValue.slice(1).toLowerCase();
    }
    setNewPassenger({ ...newPassenger, [field]: filteredValue });
  };

  const handlePassportSeriesChange = (e) => {
    const value = e.target.value.replace(/[^0-9]/g, '').slice(0, 4);
    setNewPassenger({ ...newPassenger, passportSeries: value });
  };

  const handlePassportNumberChange = (e) => {
    const value = e.target.value.replace(/[^0-9]/g, '').slice(0, 6);
    setNewPassenger({ ...newPassenger, passportNumber: value });
  };

  const handleBirthDateChange = (e) => {
    let value = e.target.value.replace(/[^0-9]/g, '').slice(0, 8);
    setNewPassenger({ ...newPassenger, birthDate: value });
  };

  const formatDisplayBirthDate = (value) => {
    if (!value) return '';
    let formatted = value;
    if (value.length > 2) {
      formatted = value.slice(0, 2) + '.' + value.slice(2);
    }
    if (value.length > 4) {
      formatted = formatted.slice(0, 5) + '.' + value.slice(4);
    }
    return formatted;
  };

  const handleSave = () => {
    const { name, surname, patronymic, passportSeries, passportNumber, gender, birthDate } = newPassenger;
    if (!name || !surname || !passportNumber || !gender || !birthDate) {
      onSave(null, 'Заполните все обязательные поля');
      return;
    }
    if (!validateNameField(name) || !validateNameField(surname) || (patronymic && !validateNameField(patronymic))) {
      onSave(null, 'Имя, фамилия и отчество должны содержать только кириллицу, начинаться с заглавной буквы, без пробелов');
      return;
    }
    if (birthDate.length !== 8) {
      onSave(null, 'Дата рождения должна содержать ровно 8 цифр (ддммгггг)');
      return;
    }

    const day = parseInt(birthDate.slice(0, 2));
    const month = parseInt(birthDate.slice(2, 4));
    const year = parseInt(birthDate.slice(4, 8));
    const birthDateObj = new Date(year, month - 1, day);

    if (
      isNaN(birthDateObj.getTime()) ||
      day < 1 || day > 31 ||
      month < 1 || month > 12 ||
      birthDateObj.getDate() !== day
    ) {
      onSave(null, 'Некорректная дата рождения');
      return;
    }

    const currentDate = new Date();
    if (year < 1860 || year > currentDate.getFullYear()) {
      onSave(null, `Год рождения должен быть между 1860 и ${currentDate.getFullYear()}`);
      return;
    }

    if (birthDateObj > currentDate) {
      onSave(null, 'Дата рождения не может быть позже текущей даты');
      return;
    }

    const formattedDate = `${day}.${month}.${year}`;
    const age = calculateAge(formattedDate);
    if (age < 14 && passportSeries) {
      onSave(null, 'Серия паспорта не должна быть указана для пассажиров младше 14 лет');
      return;
    }
    if (age >= 14 && (!passportSeries || passportSeries.length !== 4)) {
      onSave(null, 'Серия паспорта обязательна для возраста 14+ и должна содержать ровно 4 цифры');
      return;
    }
    if (passportNumber.length !== 6) {
      onSave(null, 'Номер паспорта должен содержать ровно 6 цифр');
      return;
    }

    const formattedBirthDate = `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
    const formattedGender = gender === 'Мужской' ? 'M' : 'F';
    const passengerData = {
      name,
      surname,
      patronymic: patronymic || null,
      passportSeries,
      passportNumber,
      gender: formattedGender,
      birthDate: formattedBirthDate,
    };
    onSave(passengerData, null);
  };

  useEffect(() => {
    if (!open) {
      setNewPassenger({
        name: '',
        surname: '',
        patronymic: '',
        passportSeries: '',
        passportNumber: '',
        gender: '',
        birthDate: '',
      });
    }
  }, [open]);

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
            Добавить пассажира
          </Typography>
          <TextField
            label="Фамилия"
            value={newPassenger.surname}
            onChange={(e) => handleNameFieldChange('surname', e.target.value)}
            fullWidth
            margin="normal"
            required
            error={newPassenger.surname && !validateNameField(newPassenger.surname)}
            helperText={
              newPassenger.surname && !validateNameField(newPassenger.surname)
                ? 'Только кириллица, без пробелов, первый символ заглавный'
                : ''
            }
            InputProps={{
              startAdornment: (
                <PersonIcon sx={{ color: '#1976d2', mr: 1 }} />
              ),
            }}
            sx={{
              '& .MuiOutlinedInput-root': {
                borderRadius: '10px',
                backgroundColor: '#fff',
                '&:hover fieldset': {
                  borderColor: '#42a5f5',
                },
                '&.Mui-focused fieldset': {
                  borderColor: '#1976d2',
                },
              },
              '& .MuiInputLabel-root': {
                color: '#757575',
                '&.Mui-focused': {
                  color: '#1976d2',
                },
              },
            }}
          />
          <TextField
            label="Имя"
            value={newPassenger.name}
            onChange={(e) => handleNameFieldChange('name', e.target.value)}
            fullWidth
            margin="normal"
            required
            error={newPassenger.name && !validateNameField(newPassenger.name)}
            helperText={
              newPassenger.name && !validateNameField(newPassenger.name)
                ? 'Только кириллица, без пробелов, первый символ заглавный'
                : ''
            }
            InputProps={{
              startAdornment: (
                <PersonIcon sx={{ color: '#1976d2', mr: 1 }} />
              ),
            }}
            sx={{
              '& .MuiOutlinedInput-root': {
                borderRadius: '10px',
                backgroundColor: '#fff',
                '&:hover fieldset': {
                  borderColor: '#42a5f5',
                },
                '&.Mui-focused fieldset': {
                  borderColor: '#1976d2',
                },
              },
              '& .MuiInputLabel-root': {
                color: '#757575',
                '&.Mui-focused': {
                  color: '#1976d2',
                },
              },
            }}
          />
          <TextField
            label="Отчество (если есть)"
            value={newPassenger.patronymic}
            onChange={(e) => handleNameFieldChange('patronymic', e.target.value)}
            fullWidth
            margin="normal"
            error={newPassenger.patronymic && !validateNameField(newPassenger.patronymic)}
            helperText={
              newPassenger.patronymic && !validateNameField(newPassenger.patronymic)
                ? 'Только кириллица, без пробелов, первый символ заглавный'
                : ''
            }
            InputProps={{
              startAdornment: (
                <PersonIcon sx={{ color: '#1976d2', mr: 1 }} />
              ),
            }}
            sx={{
              '& .MuiOutlinedInput-root': {
                borderRadius: '10px',
                backgroundColor: '#fff',
                '&:hover fieldset': {
                  borderColor: '#42a5f5',
                },
                '&.Mui-focused fieldset': {
                  borderColor: '#1976d2',
                },
              },
              '& .MuiInputLabel-root': {
                color: '#757575',
                '&.Mui-focused': {
                  color: '#1976d2',
                },
              },
            }}
          />
          <TextField
            label="Серия паспорта"
            value={newPassenger.passportSeries}
            onChange={handlePassportSeriesChange}
            fullWidth
            margin="normal"
            required={!isUnder14()}
            error={newPassenger.passportSeries && newPassenger.passportSeries.length !== 4}
            helperText={
              newPassenger.passportSeries && newPassenger.passportSeries.length !== 4
                ? 'Серия паспорта должна содержать ровно 4 цифры'
                : isUnder14()
                ? 'Не обязательно для возраста младше 14 лет'
                : ''
            }
            InputProps={{
              startAdornment: (
                <AssignmentIndIcon sx={{ color: '#1976d2', mr: 1 }} />
              ),
            }}
            sx={{
              '& .MuiOutlinedInput-root': {
                borderRadius: '10px',
                backgroundColor: '#fff',
                '&:hover fieldset': {
                  borderColor: '#42a5f5',
                },
                '&.Mui-focused fieldset': {
                  borderColor: '#1976d2',
                },
              },
              '& .MuiInputLabel-root': {
                color: '#757575',
                '&.Mui-focused': {
                  color: '#1976d2',
                },
              },
            }}
          />
          <TextField
            label="Номер паспорта"
            value={newPassenger.passportNumber}
            onChange={handlePassportNumberChange}
            fullWidth
            margin="normal"
            required
            error={newPassenger.passportNumber && newPassenger.passportNumber.length !== 6}
            helperText={
              newPassenger.passportNumber && newPassenger.passportNumber.length !== 6
                ? 'Номер паспорта должен содержать ровно 6 цифр'
                : ''
            }
            InputProps={{
              startAdornment: (
                <AssignmentIndIcon sx={{ color: '#1976d2', mr: 1 }} />
              ),
            }}
            sx={{
              '& .MuiOutlinedInput-root': {
                borderRadius: '10px',
                backgroundColor: '#fff',
                '&:hover fieldset': {
                  borderColor: '#42a5f5',
                },
                '&.Mui-focused fieldset': {
                  borderColor: '#1976d2',
                },
              },
              '& .MuiInputLabel-root': {
                color: '#757575',
                '&.Mui-focused': {
                  color: '#1976d2',
                },
              },
            }}
          />
          <FormHelperText sx={{ mt: -1, mb: 1, color: '#757575' }}>
            или свидетельство о рождении
          </FormHelperText>
          <Select
            value={newPassenger.gender}
            onChange={(e) => setNewPassenger({ ...newPassenger, gender: e.target.value })}
            displayEmpty
            fullWidth
            margin="normal"
            required
            startAdornment={<GenderIcon sx={{ color: '#1976d2', mr: 1 }} />}
            sx={{
              mt: 2,
              mb: 1,
              borderRadius: '10px',
              backgroundColor: '#fff',
              '&:hover fieldset': {
                borderColor: '#42a5f5',
              },
              '&.Mui-focused fieldset': {
                borderColor: '#1976d2',
              },
            }}
          >
            <MenuItem value="" disabled>
              Выберите пол
            </MenuItem>
            <MenuItem value="Мужской">Мужской</MenuItem>
            <MenuItem value="Женский">Женский</MenuItem>
          </Select>
          <TextField
            label="Дата рождения (дд.мм.гггг)"
            value={formatDisplayBirthDate(newPassenger.birthDate)}
            onChange={handleBirthDateChange}
            placeholder="ддммгггг"
            fullWidth
            margin="normal"
            required
            error={newPassenger.birthDate && newPassenger.birthDate.length !== 8}
            helperText={
              newPassenger.birthDate && newPassenger.birthDate.length !== 8
                ? 'Введите 8 цифр в формате ддммгггг'
                : 'Введите 8 цифр (ддммгггг)'
            }
            InputProps={{
              startAdornment: (
                <CalendarIcon sx={{ color: '#1976d2', mr: 1 }} />
              ),
            }}
            sx={{
              '& .MuiOutlinedInput-root': {
                borderRadius: '10px',
                backgroundColor: '#fff',
                '&:hover fieldset': {
                  borderColor: '#42a5f5',
                },
                '&.Mui-focused fieldset': {
                  borderColor: '#1976d2',
                },
              },
              '& .MuiInputLabel-root': {
                color: '#757575',
                '&.Mui-focused': {
                  color: '#1976d2',
                },
              },
            }}
          />
          {error && (
            <Typography color="error" sx={{ mt: 1, textAlign: 'center' }}>
              {error}
            </Typography>
          )}
          <Box sx={{ display: 'flex', gap: 2, mt: 3, justifyContent: 'center' }}>
            <Button
              variant="contained"
              onClick={handleSave}
              sx={{
                borderRadius: '20px',
                background: 'linear-gradient(90deg, #4CAF50 0%, #66BB6A 100%)',
                fontWeight: 600,
                '&:hover': {
                  background: 'linear-gradient(90deg, #388E3C 0%, #43A047 100%)',
                },
              }}
            >
              Сохранить
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
              Отмена
            </Button>
          </Box>
        </Box>
      </Fade>
    </Modal>
  );
};

export default AddPassengerModal;