import { Routes, Route, Navigate } from 'react-router-dom'
import LoginPage from './pages/LoginPage'
import DashboardPage from './pages/DashboardPage'
import SignUpPage from './pages/SignUpPage'
import VerifyCodePage from './pages/VerifyCodePage'
import ForgotPasswordPage from './pages/ForgotPasswordPage'
import ResetPasswordPage from './pages/ResetPasswordPage'
import SuccessPage from './pages/SuccessPage'
import GroupDetailPage from './pages/GroupDetailPage'
import CreateGroupPage from './pages/CreateGroupPage'
import JoinGroupPage from './pages/JoinGroupPage'

function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/signup" element={<SignUpPage />} />
      <Route path="/verify-code" element={<VerifyCodePage />} />
      <Route path="/forgot-password" element={<ForgotPasswordPage />} />
      <Route path="/reset-password" element={<ResetPasswordPage />} />
      <Route path="/success" element={<SuccessPage />} />
      <Route path="/" element={<DashboardPage />} />
      <Route path="/groups/create" element={<CreateGroupPage />} />
      <Route path="/groups/join" element={<JoinGroupPage />} />
      <Route path="/groups/:groupId" element={<GroupDetailPage />} />
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  )
}

export default App
