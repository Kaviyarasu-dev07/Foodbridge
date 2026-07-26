import React, { useState, useRef } from 'react';
import { toast } from 'react-hot-toast';
import api from '../services/api';

const PhotoEstimator = ({ onConfirm, onClose }) => {
  const [imagePreview, setImagePreview] = useState(null);
  const [file, setFile] = useState(null);
  const [loading, setLoading] = useState(false);
  const [estimate, setEstimate] = useState(null);
  const [dragActive, setDragActive] = useState(false);
  
  const fileInputRef = useRef(null);

  const handleDrag = (e) => {
    e.preventDefault();
    e.stopPropagation();
    if (e.type === "dragenter" || e.type === "dragover") {
      setDragActive(true);
    } else if (e.type === "dragleave") {
      setDragActive(false);
    }
  };

  const handleDrop = (e) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(false);
    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      processFile(e.dataTransfer.files[0]);
    }
  };

  const handleFileChange = (e) => {
    if (e.target.files && e.target.files[0]) {
      processFile(e.target.files[0]);
    }
  };

  const processFile = (selectedFile) => {
    // Validate file type
    const validTypes = ['image/jpeg', 'image/png', 'image/jpg'];
    if (!validTypes.includes(selectedFile.type)) {
      toast.error("Invalid file type. Only JPEG and PNG images are accepted.");
      return;
    }

    // Validate file size (5MB)
    if (selectedFile.size > 5 * 1024 * 1024) {
      toast.error("File size exceeds the limit of 5MB.");
      return;
    }

    setFile(selectedFile);
    const reader = new FileReader();
    reader.onloadend = () => {
      setImagePreview(reader.result);
    };
    reader.readAsDataURL(selectedFile);
    // Reset any previous estimate
    setEstimate(null);
  };

  const triggerFileSelect = () => {
    fileInputRef.current.click();
  };

  const handleAnalyze = async () => {
    if (!file) {
      toast.error("Please upload an image first");
      return;
    }

    setLoading(true);
    const formData = new FormData();
    formData.append('image', file);

    try {
      const response = await api.post('/api/donor/listings/estimate-photo', formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });
      setEstimate(response.data);
      toast.success("AI analysis complete!");
    } catch (err) {
      console.error(err);
      const errMsg = err.response?.data?.error || "AI estimation failed. Please try again.";
      toast.error(errMsg);
    } finally {
      setLoading(false);
    }
  };

  const handleFieldChange = (field, value) => {
    setEstimate(prev => ({
      ...prev,
      [field]: value
    }));
  };

  const handleConfirmSelection = () => {
    if (!estimate) return;
    onConfirm(estimate);
    toast.success("AI values applied to form!");
    onClose();
  };

  return (
    <div className="photo-estimator-modal" onClick={onClose}>
      <div className="photo-estimator-card" onClick={(e) => e.stopPropagation()}>
        <div className="estimator-header">
          <h3>🤖 AI Photo Food Estimator</h3>
          <button className="close-btn" onClick={onClose}>&times;</button>
        </div>

        {/* Drag and Drop Zone */}
        {!imagePreview && (
          <div 
            className={`drop-zone ${dragActive ? 'active' : ''}`}
            onDragEnter={handleDrag}
            onDragLeave={handleDrag}
            onDragOver={handleDrag}
            onDrop={handleDrop}
            onClick={triggerFileSelect}
          >
            <div className="drop-zone-content">
              <span className="upload-icon">📸</span>
              <p>Drag and drop your food photo here, or <strong>click to browse</strong></p>
              <span>Supports JPEG, PNG (Max 5MB)</span>
            </div>
            <input 
              type="file" 
              ref={fileInputRef} 
              style={{ display: 'none' }} 
              accept="image/jpeg, image/png"
              onChange={handleFileChange}
            />
          </div>
        )}

        {/* Image Preview */}
        {imagePreview && !estimate && !loading && (
          <div className="preview-container">
            <div className="preview-wrapper">
              <img src={imagePreview} alt="Food Upload Preview" />
            </div>

            <div className="action-row">
              <button className="btn btn-secondary" onClick={() => { setImagePreview(null); setFile(null); }}>
                Remove Image
              </button>
              <button className="btn btn-primary" onClick={handleAnalyze}>
                Run AI Estimation
              </button>
            </div>
          </div>
        )}

        {/* Loading Spinner */}
        {loading && (
          <div className="estimator-loading">
            <div className="spinner"></div>
            <p>AI is analysing your food...</p>
            <span>Estimating portions, food type, condition, and details...</span>
          </div>
        )}

        {/* AI Result Card */}
        {estimate && !loading && (
          <div className="estimator-result-card">
            <div className="result-img-side">
              <img src={imagePreview} alt="Analyzed Food" className="thumbnail-preview" />
              <div className="confidence-badge">
                Confidence: <span className="conf-high">{estimate.confidence}</span>
              </div>
            </div>

            <div className="result-fields">
              <h4>Estimated Details (Edit if needed)</h4>
              
              <div className="result-field-group">
                <label>Estimated Name:</label>
                <input 
                  type="text" 
                  value={estimate.foodName} 
                  onChange={(e) => handleFieldChange('foodName', e.target.value)}
                />
              </div>

              <div className="result-field-group">
                <label>Estimated Portions:</label>
                <input 
                  type="number" 
                  value={estimate.quantity} 
                  onChange={(e) => handleFieldChange('quantity', parseInt(e.target.value) || 0)}
                />
              </div>

              <div className="result-field-group">
                <label>Food Condition:</label>
                <select 
                  value={estimate.condition} 
                  onChange={(e) => handleFieldChange('condition', e.target.value)}
                >
                  <option value="FRESH">FRESH</option>
                  <option value="GOOD">GOOD</option>
                  <option value="USE_SOON">USE SOON</option>
                </select>
              </div>

              <div className="result-field-group">
                <label>Food Type:</label>
                <select 
                  value={estimate.foodType} 
                  onChange={(e) => handleFieldChange('foodType', e.target.value)}
                >
                  <option value="COOKED_MEAL">COOKED_MEAL</option>
                  <option value="SNACKS">SNACKS</option>
                  <option value="RAW">RAW</option>
                </select>
              </div>
            </div>

            <div className="result-actions">
              <button 
                className="btn btn-secondary btn-block" 
                onClick={() => { setEstimate(null); }}
              >
                Re-upload Photo
              </button>
              <button 
                className="btn btn-primary btn-block" 
                onClick={handleConfirmSelection}
              >
                Confirm & Autofill Form
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default PhotoEstimator;
