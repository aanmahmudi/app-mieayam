export function AuthCard({
  authRoute,
  registerUsername,
  setRegisterUsername,
  registerPassword,
  setRegisterPassword,
  registerStatus,
  onRegister,
  onGoToLogin,
  loginUsername,
  setLoginUsername,
  loginPassword,
  setLoginPassword,
  loginStatus,
  onLogin,
  onGoToRegister,
}) {
  return (
    <section className="card authCard">
      {authRoute === 'register' ? (
        <>
          <h2>Register</h2>
          <form className="form" onSubmit={onRegister}>
            <label className="field">
              <div className="label">Username</div>
              <input
                className="input"
                value={registerUsername}
                onChange={(e) => setRegisterUsername(e.target.value)}
                placeholder="contoh: admin"
              />
            </label>
            <label className="field">
              <div className="label">Password</div>
              <input
                className="input"
                type="password"
                value={registerPassword}
                onChange={(e) => setRegisterPassword(e.target.value)}
                placeholder="minimal 6 karakter"
              />
            </label>
            <button className="button primary" type="submit">
              Register
            </button>
            {registerStatus ? <div className="status">{registerStatus}</div> : null}
          </form>
          <div className="status">
            Sudah punya akun?{' '}
            <button className="linkButton" type="button" onClick={onGoToLogin}>
              Login
            </button>
          </div>
        </>
      ) : (
        <>
          <h2>Login</h2>
          <form className="form" onSubmit={onLogin}>
            <label className="field">
              <div className="label">Username</div>
              <input className="input" value={loginUsername} onChange={(e) => setLoginUsername(e.target.value)} />
            </label>
            <label className="field">
              <div className="label">Password</div>
              <input
                className="input"
                type="password"
                value={loginPassword}
                onChange={(e) => setLoginPassword(e.target.value)}
              />
            </label>
            <button className="button primary" type="submit">
              Login
            </button>
            {loginStatus ? <div className="status">{loginStatus}</div> : null}
          </form>
          <div className="status">
            Belum punya akun?{' '}
            <button className="linkButton" type="button" onClick={onGoToRegister}>
              Register
            </button>
          </div>
        </>
      )}
    </section>
  )
}

