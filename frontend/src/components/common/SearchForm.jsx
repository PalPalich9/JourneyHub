import { useState, useRef } from 'react';
import { Box, TextField, Typography, Button, FormControlLabel, Checkbox, FormControl, Select, MenuItem } from '@mui/material';
import calendarSvg from '../../assets/images/calendar.svg';
import clockSvg from '../../assets/images/clock.svg';
import { validateCity, normalizeCityName, validateDate, validateTime } from '../../utils/validationUtils';

const SearchForm = ({ withTransfers, setWithTransfers, multiStop, setMultiStop, selectedTransport, sortCriteria, setSortCriteria, fetchRoutes }) => {
  const [departureCity, setDepartureCity] = useState('');
  const [arrivalCity, setArrivalCity] = useState('');
  const [dateTime, setDateTime] = useState({ day: '', month: '', hours: '', minutes: '' });

  const dayRef = useRef(null);
  const monthRef = useRef(null);
  const hoursRef = useRef(null);
  const minutesRef = useRef(null);

  const handleCityInput = (e, setter) => {
    let value = e.target.value;
    if (value.length === 1 && value === ' ') return;
    let filteredValue = value.replace(/[^а-яА-Я\s-]/g, '').replace(/--+/g, '-');
    if (filteredValue.length <= 50) {
      const normalizedValue = normalizeCityName(filteredValue);
      setter(normalizedValue);
    }
  };

  const handleNumericInput = (field, e, nextRef) => {
    const value = e.target.value.replace(/[^0-9]/g, '').slice(0, 2);
    setDateTime((prev) => ({ ...prev, [field]: value }));
    if (value.length === 2 && nextRef?.current) nextRef.current.focus();
  };

  const handleDayChange = (e) => handleNumericInput('day', e, monthRef);
  const handleMonthChange = (e) => handleNumericInput('month', e, null);
  const handleHoursChange = (e) => handleNumericInput('hours', e, minutesRef);
  const handleMinutesChange = (e) => handleNumericInput('minutes', e, null);

  const handleWithTransfersChange = (e) => {
    const isChecked = e.target.checked;
    setWithTransfers(isChecked);
    if (!isChecked) {
      setMultiStop(false);
    }
  };

  const handleMultiStopChange = (e) => {
    const isChecked = e.target.checked;
    setMultiStop(isChecked);
    if (isChecked) {
      setWithTransfers(true);
    }
  };

  const handleSearch = () => {
    const today = new Date();
    const currentYear = today.getFullYear();
    const currentMonth = today.getMonth() + 1;
    const currentDay = today.getDate();
    let departureTime = '';

    const day = parseInt(dateTime.day) || null;
    const month = parseInt(dateTime.month) || null;
    const hours = parseInt(dateTime.hours) || null;
    const minutes = parseInt(dateTime.minutes) || null;

    if ((day && !month) || (!day && month)) {
      alert('Пожалуйста, введите полную дату (день и месяц)');
      return;
    }

    if (!day && !month && hours && !minutes) {
      alert('Пожалуйста, введите дату (день и месяц) для указанных часов.');
      return;
    }
    if (!day && !month && !hours && minutes) {
      alert('Пожалуйста, введите дату (день и месяц) для указанных минут.');
      return;
    }

    if (day && month) {
      let year = currentYear;
      if (month < currentMonth || (month === currentMonth && day < currentDay)) {
        year = currentYear + 1;
      }

      if (!validateDate(day, month, year)) {
        return;
      }

      const finalHours = hours !== null ? hours : 0;
      const finalMinutes = minutes !== null ? minutes : 0;

      if (hours !== null && !validateTime(finalHours, finalMinutes)) {
        return;
      }
      if (minutes !== null && !validateTime(finalHours, finalMinutes)) {
        return;
      }

      departureTime = `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}T${String(finalHours).padStart(2, '0')}:${String(finalMinutes).padStart(2, '0')}:00`;
    }

    if (!departureCity.trim() || !arrivalCity.trim()) {
      fetchRoutes(
        departureCity.trim(),
        arrivalCity.trim(),
        null,
        withTransfers,
        selectedTransport !== 'mix' ? selectedTransport : null,
        sortCriteria,
        multiStop
      );
      return;
    }

    if (!validateCity(departureCity.trim()) || !validateCity(arrivalCity.trim())) {
      alert('Поля "Откуда" и "Куда" должны содержать только кириллицу, "-" и пробел (2-50 символов, без двойного "-" и цифр)');
      return;
    }

    fetchRoutes(
      departureCity.trim(),
      arrivalCity.trim(),
      departureTime,
      withTransfers,
      selectedTransport !== 'mix' ? selectedTransport : null,
      sortCriteria,
      multiStop
    );
  };

  return (
    <Box
      sx={{
        display: 'flex',
        flexDirection: 'column',
        gap: 3,
        mb: 3,
        justifyContent: 'center',
        alignItems: 'center',
        flexWrap: 'wrap',
        width: '100%',
        mx: 'auto',
        padding: '20px',
        borderRadius: '12px',
      }}
    >
      <Box
        sx={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          gap: 3,
          flexWrap: 'nowrap',
          '@media (max-width: 900px)': {
            flexWrap: 'wrap',
            gap: 2,
          },
        }}
      >
        <TextField
          placeholder="Откуда"
          value={departureCity}
          onChange={(e) => handleCityInput(e, setDepartureCity)}
          onPaste={(e) => e.preventDefault()}
          autoComplete="off"
          variant="outlined"
          sx={{
            minWidth: '220px',
            '& .MuiOutlinedInput-root': {
              borderRadius: '8px',
              backgroundColor: '#f5f7fa',
              '& fieldset': {
                border: 'none',
              },
              '&:hover fieldset': {
                border: 'none',
              },
              '&.Mui-focused fieldset': {
                border: 'none',
              },
            },
            '& .MuiInputBase-input': {
              padding: '12px',
              fontSize: '20px',
              borderBottom: '2px solid #1976d2',
              color: '#333',
            },
          }}
        />
        <TextField
          placeholder="Куда"
          value={arrivalCity}
          onChange={(e) => handleCityInput(e, setArrivalCity)}
          onPaste={(e) => e.preventDefault()}
          autoComplete="off"
          variant="outlined"
          sx={{
            minWidth: '220px',
            '& .MuiOutlinedInput-root': {
              borderRadius: '8px',
              backgroundColor: '#f5f7fa',
              '& fieldset': {
                border: 'none',
              },
              '&:hover fieldset': {
                border: 'none',
              },
              '&.Mui-focused fieldset': {
                border: 'none',
              },
            },
            '& .MuiInputBase-input': {
              padding: '12px',
              fontSize: '20px',
              borderBottom: '2px solid #1976d2',
              color: '#333',
            },
          }}
        />
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <img src={calendarSvg} alt="Calendar" style={{ marginRight: '8px', width: '40px', height: '40px' }} />
          <TextField
            inputRef={dayRef}
            value={dateTime.day}
            onChange={handleDayChange}
            onPaste={(e) => e.preventDefault()}
            autoComplete="off"
            placeholder="ДД"
            variant="outlined"
            sx={{
              '& .MuiOutlinedInput-root': {
                borderRadius: '8px',
                backgroundColor: '#f5f7fa',
                '& fieldset': {
                  border: 'none',
                },
                '&:hover fieldset': {
                  border: 'none',
                },
                '&.Mui-focused fieldset': {
                  border: 'none',
                },
              },
              '& .MuiInputBase-input': {
                padding: '10px',
                fontSize: '18px',
                textAlign: 'center',
                width: '50px',
                borderBottom: '2px solid #1976d2',
                color: '#333',
              },
            }}
          />
          <Typography sx={{ mx: 0.5, fontSize: '18px', color: '#666' }}>.</Typography>
          <TextField
            inputRef={monthRef}
            value={dateTime.month}
            onChange={handleMonthChange}
            onPaste={(e) => e.preventDefault()}
            autoComplete="off"
            placeholder="ММ"
            variant="outlined"
            sx={{
              '& .MuiOutlinedInput-root': {
                borderRadius: '8px',
                backgroundColor: '#f5f7fa',
                '& fieldset': {
                  border: 'none',
                },
                '&:hover fieldset': {
                  border: 'none',
                },
                '&.Mui-focused fieldset': {
                  border: 'none',
                },
              },
              '& .MuiInputBase-input': {
                padding: '10px',
                fontSize: '18px',
                textAlign: 'center',
                width: '50px',
                borderBottom: '2px solid #1976d2',
                color: '#333',
              },
            }}
          />
        </Box>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <img src={clockSvg} alt="Clock" style={{ marginRight: '8px', width: '40px', height: '40px' }} />
          <TextField
            inputRef={hoursRef}
            value={dateTime.hours}
            onChange={handleHoursChange}
            onPaste={(e) => e.preventDefault()}
            autoComplete="off"
            placeholder="ЧЧ"
            variant="outlined"
            sx={{
              '& .MuiOutlinedInput-root': {
                borderRadius: '8px',
                backgroundColor: '#f5f7fa',
                '& fieldset': {
                  border: 'none',
                },
                '&:hover fieldset': {
                  border: 'none',
                },
                '&.Mui-focused fieldset': {
                  border: 'none',
                },
              },
              '& .MuiInputBase-input': {
                padding: '10px',
                fontSize: '18px',
                textAlign: 'center',
                width: '50px',
                borderBottom: '2px solid #1976d2',
                color: '#333',
              },
            }}
          />
          <Typography sx={{ mx: 0.5, fontSize: '18px', color: '#666' }}>:</Typography>
          <TextField
            inputRef={minutesRef}
            value={dateTime.minutes}
            onChange={handleMinutesChange}
            onPaste={(e) => e.preventDefault()}
            autoComplete="off"
            placeholder="ММ"
            variant="outlined"
            sx={{
              '& .MuiOutlinedInput-root': {
                borderRadius: '8px',
                backgroundColor: '#f5f7fa',
                '& fieldset': {
                  border: 'none',
                },
                '&:hover fieldset': {
                  border: 'none',
                },
                '&.Mui-focused fieldset': {
                  border: 'none',
                },
              },
              '& .MuiInputBase-input': {
                padding: '10px',
                fontSize: '18px',
                textAlign: 'center',
                width: '50px',
                borderBottom: '2px solid #1976d2',
                color: '#333',
              },
            }}
          />
        </Box>
      </Box>
      <Box
        sx={{
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center',
          gap: 4,
          mt: 2,
          flexWrap: 'wrap',
          width: '100%',
        }}
      >
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
          <FormControlLabel
            control={<Checkbox checked={withTransfers} onChange={handleWithTransfersChange} />}
            label="С пересадками"
            sx={{ '& .MuiTypography-root': { fontSize: '18px', color: '#333' } }}
          />
          <FormControlLabel
            control={<Checkbox checked={multiStop} onChange={handleMultiStopChange} />}
            label="2+ пересадки"
            sx={{ '& .MuiTypography-root': { fontSize: '18px', color: '#333' } }}
          />
        </Box>
        <FormControl sx={{ minWidth: 200 }}>
          <Select
            value={sortCriteria}
            onChange={(e) => setSortCriteria(e.target.value)}
            displayEmpty
            sx={{
              height: '50px',
              fontSize: '18px',
              borderRadius: '8px',
              backgroundColor: '#f5f7fa',
              '& .MuiOutlinedInput-notchedOutline': {
                borderColor: '#1976d2',
              },
              '&:hover .MuiOutlinedInput-notchedOutline': {
                borderColor: '#115293',
              },
              '&.Mui-focused .MuiOutlinedInput-notchedOutline': {
                borderColor: '#115293',
              },
            }}
          >
            <MenuItem value="DEFAULT">По времени</MenuItem>
            <MenuItem value="CHEAPEST">Дешевле</MenuItem>
            <MenuItem value="EXPENSIVE">Дороже</MenuItem>
            <MenuItem value="DURATION">Быстрее</MenuItem>
            <MenuItem value="AVAILABILITY">В наличии</MenuItem>
          </Select>
        </FormControl>
        <Button
          variant="contained"
          color="primary"
          onClick={handleSearch}
          sx={{
            fontSize: '18px',
            padding: '12px 32px',
            borderRadius: '8px',
            textTransform: 'none',
            backgroundColor: '#1976d2',
            '&:hover': {
              backgroundColor: '#115293',
              boxShadow: '0 2px 8px rgba(0, 0, 0, 0.2)',
            },
            transition: 'all 0.3s ease',
          }}
        >
          Искать
        </Button>
      </Box>
    </Box>
  );
};

export default SearchForm;