import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';
import { Box, Button, TextField, Typography, Tabs, Tab, Paper, InputAdornment, Fade } from '@mui/material';
import { Person as PersonIcon, Email as EmailIcon, Lock as LockIcon, Home as HomeIcon, AccountCircle as AccountCircleIcon } from '@mui/icons-material';
import * as Yup from 'yup';
import { Formik, Form, Field } from 'formik';
import api from '../api/api';

function Auth() {
  const [isLogin, setIsLogin] = useState(true);
  const navigate = useNavigate();
  const { login, register, isAuthenticated } = useAuth();

  const loginValidationSchema = Yup.object({
    email: Yup.string()
      .matches(/^[A-Za-z0-9._+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/, 'Неверный формат email')
      .test('no-cyrillic', 'Кириллица запрещена', (value) => !/[а-яА-Я]/.test(value))
      .test('no-comma', 'Запятые запрещены', (value) => !/,/.test(value))
      .test('valid-dots', 'Точка не может быть в начале, конце или повторяться', (value) => {
        if (!value) return true;
        const localPart = value.split('@')[0];
        return !/^\.|^\.\.|.*\.$|.*\.\./.test(localPart);
      })
      .max(250, 'Максимум 250 символов')
      .required('Обязательное поле')
      .email('Неверный формат email'),

    password: Yup.string()
      .min(6, 'Минимум 6 символов')
      .required('Обязательное поле'),
  });

  const registerValidationSchema = Yup.object({
    surname: Yup.string()
      .matches(/^[A-Za-zА-Яа-яЁё\s-]+$/, 'Только латиница, кириллица, пробелы и дефис')
      .test('no-numbers', 'Цифры и спецсимволы запрещены', (value) => !/[0-9!@#$%^&*(),.;:"'{}|<>?`~\[\]\\]/.test(value))
      .test('capitalized', 'Должно начинаться с заглавной буквы', (value) => {
        if (!value) return true;
        const words = value.split(/\s+/);
        return words.every(word => /^[A-ZА-Я]/.test(word));
      })
      .max(50, 'Максимум 50 символов')
      .required('Обязательное поле'),

    name: Yup.string()
      .matches(/^[A-Za-zА-Яа-яЁё\s-]+$/, 'Только латиница, кириллица, пробелы и дефис')
      .test('no-numbers', 'Цифры и спецсимволы запрещены', (value) => !/[0-9!@#$%^&*(),.;:"'{}|<>?`~\[\]\\]/.test(value))
      .test('capitalized', 'Должно начинаться с заглавной буквы', (value) => {
        if (!value) return true;
        const words = value.split(/\s+/);
        return words.every(word => /^[A-ZА-Я]/.test(word));
      })
      .max(50, 'Максимум 50 символов')
      .required('Обязательное поле'),

    email: Yup.string()
      .matches(/^[A-Za-z0-9._+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/, 'Неверный формат email')
      .test('no-cyrillic', 'Кириллица запрещена', (value) => !/[а-яА-Я]/.test(value))
      .test('no-comma', 'Запятые запрещены', (value) => !/,/.test(value))
      .test('valid-dots', 'Точка не может быть в начале, конце или повторяться', (value) => {
        if (!value) return true;
        const localPart = value.split('@')[0];
        return !/^\.|^\.\.|.*\.$|.*\.\./.test(localPart);
      })
      .max(250, 'Максимум 250 символов')
      .required('Обязательное поле')
      .email('Неверный формат email'),

    password: Yup.string()
      .min(6, 'Минимум 6 символов')
      .required('Обязательное поле'),

    confirmPassword: Yup.string()
      .oneOf([Yup.ref('password'), null], 'Пароли должны совпадать')
      .required('Обязательное поле'),
  });

  const handleLoginSubmit = async (values, { setSubmitting, setFieldError }) => {
    try {
      const response = await api.post('/auth/login', {
        email: values.email,
        password: values.password,
      });
      const { token } = response.data;
      login(token, values.email);
      navigate('/user');
    } catch (err) {
      if (err.response && err.response.data) {
        const { errorCode } = err.response.data;
        switch (errorCode) {
          case 'USER_NOT_FOUND':
            setFieldError('email', 'Аккаунта с таким email не существует');
            break;
          case 'INVALID_PASSWORD':
            setFieldError('password', 'Неверный пароль');
            break;
          case 'INTERNAL_ERROR':
            setFieldError('password', 'Произошла ошибка на сервере. Попробуйте позже.');
            break;
          default:
            setFieldError('password', 'Ошибка входа. Проверьте данные.');
        }
      } else {
        setFieldError('password', 'Не удалось подключиться к серверу.');
      }
    }
    setSubmitting(false);
  };

  const handleRegisterSubmit = async (values, { setSubmitting, setFieldError }) => {
    try {
      const response = await api.post('/auth/register', {
        surname: values.surname,
        name: values.name,
        email: values.email,
        password: values.password,
      });
      const userData = response.data;
      register(userData);
      navigate('/user');
    } catch (err) {
      if (err.response && err.response.data) {
        const { errorCode } = err.response.data;
        switch (errorCode) {
          case 'DUPLICATE_EMAIL':
            setFieldError('email', 'Такой email уже существует');
            break;
          case 'VALIDATION_ERROR':
            setFieldError('email', 'Проверьте правильность введённых данных');
            break;
          case 'INTERNAL_ERROR':
            setFieldError('email', 'Произошла ошибка на сервере. Попробуйте позже.');
            break;
          default:
            setFieldError('email', 'Ошибка регистрации. Проверьте данные.');
        }
      } else {
        setFieldError('email', 'Не удалось подключиться к серверу.');
      }
    }
    setSubmitting(false);
  };

  const toggleAuthMode = (event, newValue) => {
    setIsLogin(newValue === 0);
  };

  return (
    <Box
      sx={{
        minHeight: '100vh',
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'center',
        alignItems: 'center',
        textAlign: 'center',
        background: 'linear-gradient(135deg, #1976d2 0%, #42a5f5 100%)',
        width: '100vw',
      }}
    >
      <Box
        sx={{
          minHeight: '100vh',
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center',
          background: 'linear-gradient(135deg, #1976d2 0%, #42a5f5 100%)',
          padding: 10,
          position: 'relative',
          overflow: 'hidden',
          '&:before': {
            content: '""',
            position: 'absolute',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            background: 'rgba(0, 0, 0, 0.3)',
            zIndex: 1,
          },
        }}
      >
        <Box sx={{ position: 'relative', zIndex: 2, width: '100%', maxWidth: '1200px' }}>
          <Fade in={true} timeout={1000}>
            <Box sx={{ textAlign: 'center', mb: 4 }}>
              <Typography
                variant="h3"
                sx={{
                  fontWeight: 700,
                  color: '#fff',
                  textShadow: '2px 2px 4px rgba(0,0,0,0.3)',
                  letterSpacing: '2px',
                }}
              >
                JourneyHub
              </Typography>
            </Box>
          </Fade>

          <Box sx={{ display: 'flex', justifyContent: 'center', mb: 4 }}>
            <Button
              variant="contained"
              startIcon={<HomeIcon />}
              onClick={() => navigate('/')}
              sx={{
                borderRadius: '20px',
                background: 'linear-gradient(90deg, #fff 0%, #e0e0e0 100%)',
                color: '#1976d2',
                fontWeight: 600,
                mr: 2,
                '&:hover': {
                  background: 'linear-gradient(90deg, #e0e0e0 0%, #fff 100%)',
                },
              }}
            >
              На главную
            </Button>
            <Button
              variant="outlined"
              startIcon={<AccountCircleIcon />}
              onClick={() => navigate('/user', { state: { tab: 0 } })}
              disabled={!isAuthenticated}
              sx={{
                borderRadius: '20px',
                borderColor: '#fff',
                color: '#fff',
                fontWeight: 600,
                '&:hover': {
                  borderColor: '#e0e0e0',
                  backgroundColor: 'rgba(255, 255, 255, 0.1)',
                },
              }}
            >
              Профиль
            </Button>
          </Box>

          <Fade in={true} timeout={1500}>
            <Paper
              sx={{
                maxWidth: '450px',
                mx: 'auto',
                p: 4,
                borderRadius: '20px',
                background: 'linear-gradient(135deg, #ffffff 0%, #f5f7fa 100%)',
                boxShadow: '0 12px 32px rgba(0,0,0,0.2)',
                transition: 'transform 0.3s ease',
                '&:hover': {
                  transform: 'translateY(-5px)',
                },
              }}
            >
              <Tabs
                value={isLogin ? 0 : 1}
                onChange={toggleAuthMode}
                centered
                sx={{
                  mb: 3,
                  '& .MuiTab-root': {
                    fontSize: '1.1rem',
                    fontWeight: 600,
                    color: '#1976d2',
                    textTransform: 'none',
                  },
                  '& .Mui-selected': {
                    color: '#42a5f5',
                  },
                  '& .MuiTabs-indicator': {
                    backgroundColor: '#42a5f5',
                  },
                }}
              >
                <Tab label="Вход" />
                <Tab label="Регистрация" />
              </Tabs>

              {isLogin ? (
                <Box sx={{ width: '100%' }}>
                  <Formik
                    initialValues={{ email: '', password: '' }}
                    validationSchema={loginValidationSchema}
                    onSubmit={handleLoginSubmit}
                  >
                    {({ errors, touched, isSubmitting }) => (
                      <Form>
                        <Field
                          as={TextField}
                          name="email"
                          label="E-mail"
                          fullWidth
                          margin="normal"
                          error={touched.email && !!errors.email}
                          helperText={touched.email && errors.email}
                          InputProps={{
                            startAdornment: (
                              <InputAdornment position="start">
                                <EmailIcon sx={{ color: '#1976d2' }} />
                              </InputAdornment>
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
                        <Field
                          as={TextField}
                          name="password"
                          label="Пароль"
                          type="password"
                          fullWidth
                          margin="normal"
                          error={touched.password && !!errors.password}
                          helperText={touched.password && errors.password}
                          InputProps={{
                            startAdornment: (
                              <InputAdornment position="start">
                                <LockIcon sx={{ color: '#1976d2' }} />
                              </InputAdornment>
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
                        <Button
                          type="submit"
                          variant="contained"
                          fullWidth
                          disabled={isSubmitting}
                          sx={{
                            borderRadius: '20px',
                            background: 'linear-gradient(90deg, #1976d2 0%, #42a5f5 100%)',
                            fontSize: '1.1rem',
                            fontWeight: 600,
                            py: 1.5,
                            mt: 2,
                            textTransform: 'none',
                            '&:hover': {
                              background: 'linear-gradient(90deg, #1565c0 0%, #2196f3 100%)',
                            },
                          }}
                        >
                          Войти
                        </Button>
                      </Form>
                    )}
                  </Formik>
                </Box>
              ) : (
                <Box sx={{ width: '100%' }}>
                  <Formik
                    initialValues={{ surname: '', name: '', email: '', password: '', confirmPassword: '' }}
                    validationSchema={registerValidationSchema}
                    onSubmit={handleRegisterSubmit}
                  >
                    {({ errors, touched, isSubmitting }) => (
                      <Form>
                        <Field
                          as={TextField}
                          name="surname"
                          label="Фамилия"
                          fullWidth
                          margin="normal"
                          error={touched.surname && !!errors.surname}
                          helperText={touched.surname && errors.surname}
                          InputProps={{
                            startAdornment: (
                              <InputAdornment position="start">
                                <PersonIcon sx={{ color: '#1976d2' }} />
                              </InputAdornment>
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
                        <Field
                          as={TextField}
                          name="name"
                          label="Имя"
                          fullWidth
                          margin="normal"
                          error={touched.name && !!errors.name}
                          helperText={touched.name && errors.name}
                          InputProps={{
                            startAdornment: (
                              <InputAdornment position="start">
                                <PersonIcon sx={{ color: '#1976d2' }} />
                              </InputAdornment>
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
                        <Field
                          as={TextField}
                          name="email"
                          label="E-mail"
                          fullWidth
                          margin="normal"
                          error={touched.email && !!errors.email}
                          helperText={touched.email && errors.email}
                          InputProps={{
                            startAdornment: (
                              <InputAdornment position="start">
                                <EmailIcon sx={{ color: '#1976d2' }} />
                              </InputAdornment>
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
                        <Field
                          as={TextField}
                          name="password"
                          label="Пароль"
                          type="password"
                          fullWidth
                          margin="normal"
                          error={touched.password && !!errors.password}
                          helperText={touched.password && errors.password}
                          InputProps={{
                            startAdornment: (
                              <InputAdornment position="start">
                                <LockIcon sx={{ color: '#1976d2' }} />
                              </InputAdornment>
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
                        <Field
                          as={TextField}
                          name="confirmPassword"
                          label="Повторите пароль"
                          type="password"
                          fullWidth
                          margin="normal"
                          error={touched.confirmPassword && !!errors.confirmPassword}
                          helperText={touched.confirmPassword && errors.confirmPassword}
                          InputProps={{
                            startAdornment: (
                              <InputAdornment position="start">
                                <LockIcon sx={{ color: '#1976d2' }} />
                              </InputAdornment>
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
                        <Button
                          type="submit"
                          variant="contained"
                          fullWidth
                          disabled={isSubmitting}
                          sx={{
                            borderRadius: '20px',
                            background: 'linear-gradient(90deg, #1976d2 0%, #42a5f5 100%)',
                            fontSize: '1.1rem',
                            fontWeight: 600,
                            py: 1.5,
                            mt: 2,
                            textTransform: 'none',
                            '&:hover': {
                              background: 'linear-gradient(90deg, #1565c0 0%, #2196f3 100%)',
                            },
                          }}
                        >
                          Зарегистрироваться
                        </Button>
                      </Form>
                    )}
                  </Formik>
                </Box>
              )}
            </Paper>
          </Fade>
        </Box>
      </Box>
    </Box>
  );
}

export default Auth;