export const normalizeCityName = (value) => {
  return value
    .split(/([-\s])/)
    .map((part, index) => {
      if (part.match(/[-\s]/)) return part;
      return part.charAt(0).toUpperCase() + part.slice(1).toLowerCase();
    })
    .join('');
};

export const validateCity = (value) => {
  const cityRegex = /^([а-яА-Я]([а-яА-Я\s-]*[а-яА-Я])?){2,50}$/;
  const doubleDashRegex = /--/;
  return cityRegex.test(value) && !doubleDashRegex.test(value);
};

export const validateDate = (day, month, year) => {
  const inputDate = new Date(year, month - 1, day);
  if (isNaN(inputDate.getTime())) return false;
  if (day < 1 || day > 31) {
    alert(`Неверная дата: ${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')} (День должен быть от 1 до 31)`);
    return false;
  }
  if (month < 1 || month > 12) {
    alert(`Неверная дата: ${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')} (Месяц должен быть от 1 до 12)`);
    return false;
  }
  const today = new Date();
  if (month < today.getMonth() + 1 || (month === today.getMonth() + 1 && day < today.getDate())) {
    inputDate.setFullYear(year + 1);
  }
  return inputDate > today;
};

export const validateTime = (hours, minutes) => {
  if (hours < 0 || hours > 23) {
    alert(`Неверное время: ${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')} (Часы должны быть от 0 до 23)`);
    return false;
  }
  if (minutes < 0 || minutes > 59) {
    alert(`Неверное время: ${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')} (Минуты должны быть от 0 до 59)`);
    return false;
  }
  return true;
};