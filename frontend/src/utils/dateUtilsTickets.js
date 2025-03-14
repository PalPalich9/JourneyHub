export const formatDate = (dateString, format = 'dd MMMM') => {
  const date = new Date(dateString);
  const day = String(date.getDate()).padStart(2, '0');
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const year = date.getFullYear();
  const monthNames = [
    'янв', 'фев', 'мар', 'апр', 'мая', 'июня',
    'июля', 'авг', 'сен', 'окт', 'ноя', 'дек'
  ];
  const fullMonth = monthNames[date.getMonth()];
  const dayWithoutLeadingZero = parseInt(day, 10);

  if (format === 'dd MMMM') {
    return `${dayWithoutLeadingZero} ${fullMonth}`;
  }
  return `${day}.${month}`;
};

export const formatTime = (dateString) => {
  const date = new Date(dateString);
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  return `${hours}:${minutes}`;
};

export const calculateDuration = (departureTime, arrivalTime, travelDuration) => {
  if (travelDuration) {
    const [hours, minutes] = travelDuration.split(':').map(Number);
    const totalMinutes = hours * 60 + minutes;
    const days = Math.floor(totalMinutes / (60 * 24));
    const remainingHours = Math.floor((totalMinutes % (60 * 24)) / 60);
    const remainingMinutes = totalMinutes % 60;

    if (days > 0) {
      return `${days} ${remainingHours} ${remainingMinutes}`;
    } else if (remainingHours > 0) {
      return `0 ${remainingHours} ${remainingMinutes}`;
    } else {
      return `0 0 ${remainingMinutes}`;
    }
  }

  const departure = new Date(departureTime);
  const arrival = new Date(arrivalTime);

  if (isNaN(departure.getTime()) || isNaN(arrival.getTime())) {
    console.error('Invalid date:', { departureTime, arrivalTime });
    return '0 0 0';
  }

  const diffMs = arrival - departure;
  if (diffMs <= 0) {
    console.warn('Arrival time is not later than departure time:', { departureTime, arrivalTime });
    return '0 0 0';
  }

  const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));
  const diffHours = Math.floor((diffMs % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
  const diffMinutes = Math.floor((diffMs % (1000 * 60 * 60)) / (1000 * 60));
  return `${diffDays} ${diffHours} ${diffMinutes}`;
};

export const formatTicketDuration = (durationStr) => {
  const [days, hours, minutes] = durationStr.split(' ').map(Number);
  return days > 0 ? `${days}д ${hours}ч ${minutes}мин` :
    hours > 0 ? `${hours}ч ${minutes}мин` : `${minutes}мин`;
};