import { useState, useEffect } from 'react';
import { Box, Typography, Button, IconButton, Fade } from '@mui/material';
import { Person } from '@mui/icons-material';
import { ArrowBackIosNew, ArrowForwardIos } from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';
import { useRoutes } from '../hooks/useRoutes';
import { formatDate } from '../utils/dateUtils';
import TransportFilters from '../components/common/TransportFilters';
import SearchForm from '../components/common/SearchForm';
import RouteCard from '../components/common/RouteCard';
import ErrorBoundary from '../error/ErrorBoundary';

function Home() {
  const [withTransfers, setWithTransfers] = useState(false);
  const [multiStop, setMultiStop] = useState(false);
  const [selectedTransport, setSelectedTransport] = useState('mix');
  const [sortCriteria, setSortCriteria] = useState('DEFAULT');
  const { routes, loading, error, fetchRoutes } = useRoutes();
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();

  const [currentDate, setCurrentDate] = useState(null);
  const [fade, setFade] = useState(true);

  useEffect(() => {
    if (routes?.type === 'direct-by-date' && routes.data) {
      const dates = Object.keys(routes.data).filter((date) => routes.data[date]?.length > 0);
      if (dates.length > 0) {
        setCurrentDate(dates[0]);
      }
    }
  }, [routes]);

  const handleTransportChange = (transport) => {
    setSelectedTransport(transport);
  };

  const hasRoutesInDirectByDate = routes?.type === 'direct-by-date' &&
    Object.entries(routes.data || {}).some(([_, routeList]) => routeList?.length > 0);

  const handlePreviousDay = () => {
    const dates = Object.keys(routes.data).filter((date) => routes.data[date]?.length > 0).sort();
    const currentIndex = dates.indexOf(currentDate);
    if (currentIndex > 0) {
      setFade(false);
      setTimeout(() => {
        setCurrentDate(dates[currentIndex - 1]);
        setFade(true);
      }, 300);
    }
  };

  const handleNextDay = () => {
    const dates = Object.keys(routes.data).filter((date) => routes.data[date]?.length > 0).sort();
    const currentIndex = dates.indexOf(currentDate);
    if (currentIndex < dates.length - 1) {
      setFade(false);
      setTimeout(() => {
        setCurrentDate(dates[currentIndex + 1]);
        setFade(true);
      }, 300);
    }
  };

  const getNavigationAvailability = () => {
    if (!routes?.data || !currentDate) return { hasPrevious: false, hasNext: false };
    const dates = Object.keys(routes.data).filter((date) => routes.data[date]?.length > 0).sort();
    const currentIndex = dates.indexOf(currentDate);
    return {
      hasPrevious: currentIndex > 0,
      hasNext: currentIndex < dates.length - 1,
    };
  };

  const { hasPrevious, hasNext } = getNavigationAvailability();

  return (
    <Box
      sx={{
        minHeight: '100vh',
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'center',
        alignItems: 'center',
        textAlign: 'center',
        width: '100vw',
      }}
    >
      <ErrorBoundary fallback={<Typography color="error">Произошла ошибка, попробуйте позже</Typography>}>
        <Box
          sx={{
            minHeight: '100vh',
            display: 'flex',
            flexDirection: 'column',
            justifyContent: 'center',
            alignItems: 'center',
            textAlign: 'center',
            width: '100%',
            backgroundColor: '#f5f7fa',
            padding: { xs: '20px 10px', sm: '30px 20px' },
          }}
        >
          <Box
            sx={{
              maxWidth: '1000px',
              width: '100%',
              margin: '0 auto',
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              backgroundColor: '#ffffff',
              borderRadius: '15px',
              boxShadow: '0 8px 16px rgba(0, 0, 0, 0.1)',
              padding: '20px',
            }}
          >
            <Typography
              variant="h2"
              sx={{
                mb: 4,
                color: '#1976d2',
                fontSize: { xs: '36px', sm: '48px' },
                fontWeight: 'bold',
                letterSpacing: '1px',
              }}
            >
              JourneyHub
            </Typography>

            <Box
              sx={{
                display: 'flex',
                justifyContent: 'center',
                alignItems: 'center',
                gap: 3,
                mb: 4,
                width: '100%',
                flexWrap: 'wrap',
              }}
            >
              <TransportFilters selectedTransport={selectedTransport} onTransportChange={handleTransportChange} />
              <Button
                variant="outlined"
                color="primary"
                startIcon={<Person />}
                onClick={() => navigate(isAuthenticated ? '/user' : '/auth')}
                sx={{
                  fontSize: '18px',
                  padding: '12px 24px',
                  minHeight: '60px',
                  minWidth: '160px',
                  borderRadius: '8px',
                  textTransform: 'none',
                  '&:hover': {
                    backgroundColor: '#e3f2fd',
                  },
                  transition: 'all 0.3s ease',
                }}
              >
                Profile
              </Button>
            </Box>

            <Box
              sx={{
                display: 'flex',
                flexDirection: 'column',
                gap: 3,
                width: '100%',
                alignItems: 'center',
              }}
            >
              <Box sx={{ width: '100%', display: 'flex', justifyContent: 'center' }}>
                <SearchForm
                  withTransfers={withTransfers}
                  setWithTransfers={setWithTransfers}
                  multiStop={multiStop}
                  setMultiStop={setMultiStop}
                  selectedTransport={selectedTransport}
                  sortCriteria={sortCriteria}
                  setSortCriteria={setSortCriteria}
                  fetchRoutes={fetchRoutes}
                />
              </Box>

              {loading && <Typography sx={{ textAlign: 'center', fontSize: '20px', color: '#666' }}>Загрузка...</Typography>}
              {error && <Typography sx={{ textAlign: 'center', fontSize: '20px', color: '#d32f2f' }}>Ошибка: {error}</Typography>}
              {routes && (
                <Box
                  sx={{
                    width: '100%',
                    mx: 'auto',
                    textAlign: 'center',
                  }}
                >
                  {routes.type === 'search' && (
                    <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 3, width: '100%' }}>
                      {routes.data?.length === 0 ? (
                        <Typography
                          variant="h1"
                          sx={{
                            color: '#757575',
                            fontSize: { xs: '40px', sm: '60px' },
                            fontWeight: 'bold',
                            mt: 4,
                          }}
                        >
                          Упс, ничего не нашли
                        </Typography>
                      ) : (
                        <Box sx={{ width: '100%', maxWidth: '800px', mx: 'auto', display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 3 }}>
                          {routes.data.map((route, index) => (
                            <RouteCard key={index} route={route} />
                          ))}
                        </Box>
                      )}
                    </Box>
                  )}
                  {routes.type === 'direct-by-date' && (
                    <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 3 }}>
                      {!hasRoutesInDirectByDate ? (
                        <Typography
                          variant="h1"
                          sx={{
                            color: '#757575',
                            fontSize: { xs: '40px', sm: '60px' },
                            fontWeight: 'bold',
                            mt: 4,
                          }}
                        >
                          Упс, ничего не нашли
                        </Typography>
                      ) : (
                        <Box sx={{ width: '100%', position: 'relative' }}>
                          {hasPrevious && (
                            <IconButton
                              onClick={handlePreviousDay}
                              sx={{
                                position: 'absolute',
                                left: { xs: '-10px', sm: '-40px' },
                                top: '50%',
                                transform: 'translateY(-50%)',
                                backgroundColor: '#1976d2',
                                color: '#fff',
                                width: { xs: 40, sm: 50 },
                                height: { xs: 40, sm: 50 },
                                '&:hover': {
                                  backgroundColor: '#115293',
                                  transform: 'translateY(-50%) scale(1.1)',
                                },
                                transition: 'all 0.3s ease',
                                boxShadow: '0 4px 8px rgba(0, 0, 0, 0.1)',
                              }}
                            >
                              <ArrowBackIosNew sx={{ fontSize: { xs: 20, sm: 24 } }} />
                            </IconButton>
                          )}

                          <Fade in={fade} timeout={300}>
                            <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 3 }}>
                              {currentDate && routes.data[currentDate]?.length > 0 ? (
                                <>
                                  <Typography
                                    variant="h5"
                                    sx={{
                                      mb: 2,
                                      mt: 3,
                                      color: '#1976d2',
                                      fontSize: { xs: '20px', sm: '24px' },
                                      fontWeight: 'bold',
                                      backgroundColor: '#e3f2fd',
                                      padding: '8px 16px',
                                      borderRadius: '8px',
                                      boxShadow: '0 2px 4px rgba(0, 0, 0, 0.1)',
                                    }}
                                  >
                                    {formatDate(currentDate)}
                                  </Typography>
                                  {routes.data[currentDate].map((route) => (
                                    <RouteCard key={route.ID} route={route} />
                                  ))}
                                </>
                              ) : (
                                <Typography
                                  variant="h6"
                                  sx={{
                                    color: '#757575',
                                    fontSize: { xs: '18px', sm: '22px' },
                                    mt: 4,
                                    fontStyle: 'italic',
                                  }}
                                >
                                  На эту дату маршрутов нет
                                </Typography>
                              )}
                            </Box>
                          </Fade>

                          {hasNext && (
                            <IconButton
                              onClick={handleNextDay}
                              sx={{
                                position: 'absolute',
                                right: { xs: '-10px', sm: '-40px' },
                                top: '50%',
                                transform: 'translateY(-50%)',
                                backgroundColor: '#1976d2',
                                color: '#fff',
                                width: { xs: 40, sm: 50 },
                                height: { xs: 40, sm: 50 },
                                '&:hover': {
                                  backgroundColor: '#115293',
                                  transform: 'translateY(-50%) scale(1.1)',
                                },
                                transition: 'all 0.3s ease',
                                boxShadow: '0 4px 8px rgba(0, 0, 0, 0.1)',
                              }}
                            >
                              <ArrowForwardIos sx={{ fontSize: { xs: 20, sm: 24 } }} />
                            </IconButton>
                          )}
                        </Box>
                      )}
                    </Box>
                  )}
                </Box>
              )}
            </Box>
          </Box>
        </Box>
      </ErrorBoundary>
    </Box>
  );
}

export default Home;