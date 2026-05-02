import axios from 'axios';

export const uploadFile = async (formData) => {
  return axios.post('/api/files/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
};

export const getFilesByRelation = async (relatedType, relatedId) => {
  return axios.get('/api/files/by-relation', {
    params: { relatedType, relatedId },
  });
};

export const downloadFile = async (id) => {
  return axios.get(`/api/files/download/${id}`, {
    responseType: 'blob',
  });
};
