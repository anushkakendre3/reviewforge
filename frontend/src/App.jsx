import { useState } from "react";
import ReactMarkdown from "react-markdown";
import "./App.css";

// ==========================================
// BACKEND API URL
// ==========================================

const API_URL =
  import.meta.env.VITE_API_URL ||
  "http://localhost:8080";

// ==========================================
// CLEAN AI REVIEW OUTPUT
// ==========================================

function cleanReview(review) {
  if (!review) {
    return "No review findings were returned.";
  }

  if (typeof review !== "string") {
    return JSON.stringify(review, null, 2);
  }

  return review
    .replace(/\\n/g, "\n")
    .replace(/\\"/g, '"')
    .replace(/\\`/g, "`")
    .replace(/\\\*/g, "*")
    .replace(/\\#/g, "#");
}

// ==========================================
// FILE ICON HELPER
// ==========================================

function getFileIcon(file) {
  const lower = file.toLowerCase();

  if (lower.endsWith(".py")) return "PY";
  if (lower.endsWith(".java")) return "JA";
  if (lower.endsWith(".js")) return "JS";
  if (lower.endsWith(".jsx")) return "RX";
  if (lower.endsWith(".ts")) return "TS";
  if (lower.endsWith(".tsx")) return "TX";
  if (lower.endsWith(".cpp")) return "C+";
  if (lower.endsWith(".c")) return "C";
  if (lower.endsWith(".cs")) return "C#";
  if (lower.endsWith(".go")) return "GO";
  if (lower.endsWith(".rs")) return "RS";
  if (lower.endsWith(".css")) return "CS";
  if (lower.endsWith(".html")) return "HT";
  if (lower.endsWith(".json")) return "JN";
  if (lower.endsWith(".md")) return "MD";

  return "•";
}

// ==========================================
// APP
// ==========================================

function App() {

  // ==========================================
  // SECURITY CODE STATE
  // ==========================================

  const [securityVerified, setSecurityVerified] =
    useState(
      sessionStorage.getItem("securityVerified") === "true"
    );

  const [securityCode, setSecurityCode] =
    useState("");

  const [securityLoading, setSecurityLoading] =
    useState(false);

  const [securityMessage, setSecurityMessage] =
    useState("");

  // ==========================================
  // AUTH STATE
  // ==========================================

  const [isLogin, setIsLogin] =
    useState(true);

  const [name, setName] =
    useState("");

  const [email, setEmail] =
    useState("");

  const [password, setPassword] =
    useState("");

  const [token, setToken] =
    useState(
      localStorage.getItem("token")
    );

  // ==========================================
  // REVIEW STATE
  // ==========================================

  const [repoUrl, setRepoUrl] =
    useState("");

  const [result, setResult] =
    useState(null);

  const [loading, setLoading] =
    useState(false);

  const [message, setMessage] =
    useState("");

  // ==========================================
  // VERIFY SECURITY CODE
  // ==========================================

  const verifySecurityCode = async () => {

    setSecurityMessage("");

    if (!securityCode.trim()) {

      setSecurityMessage(
        "Please enter the security code."
      );

      return;
    }

    setSecurityLoading(true);

    try {

      const response = await fetch(
        `${API_URL}/verify-security-code`,
        {
          method: "POST",

          headers: {
            "Content-Type": "application/json",
          },

          body: JSON.stringify({
            securityCode: securityCode.trim(),
          }),
        }
      );

      const text = await response.text();

      let data;

      try {
        data = JSON.parse(text);
      } catch {
        data = {
          message: text,
        };
      }

      if (!response.ok) {

        throw new Error(
          data.message ||
          data.error ||
          "Invalid security code."
        );
      }

      sessionStorage.setItem(
        "securityVerified",
        "true"
      );

      setSecurityVerified(true);

      setSecurityCode("");

      setSecurityMessage("");

    } catch (error) {

      console.error(
        "Security code error:",
        error
      );

      setSecurityMessage(
        error.message ||
        "Unable to verify security code."
      );

    } finally {

      setSecurityLoading(false);

    }
  };

  // ==========================================
  // LOGIN / REGISTER
  // ==========================================

  const handleAuth = async () => {

    setMessage("");

    if (!email.trim()) {

      setMessage(
        "Please enter your email."
      );

      return;
    }

    if (!password.trim()) {

      setMessage(
        "Please enter your password."
      );

      return;
    }

    if (!isLogin && !name.trim()) {

      setMessage(
        "Please enter your name."
      );

      return;
    }

    setLoading(true);

    try {

      const url = isLogin
        ? `${API_URL}/login`
        : `${API_URL}/users`;

      const body = isLogin
        ? {
            email: email.trim(),
            password: password,
          }
        : {
            name: name.trim(),
            email: email.trim(),
            password: password,
          };

      const response = await fetch(
        url,
        {
          method: "POST",

          headers: {
            "Content-Type":
              "application/json",
          },

          body:
            JSON.stringify(body),
        }
      );

      const text =
        await response.text();

      if (!response.ok) {

        let errorMessage =
          "Request failed.";

        try {

          const data =
            JSON.parse(text);

          errorMessage =
            data.error ||
            data.message ||
            errorMessage;

        } catch {

          if (text.trim()) {

            errorMessage =
              text.trim();
          }
        }

        throw new Error(
          errorMessage
        );
      }

      // ======================================
      // LOGIN SUCCESS
      // ======================================

      if (isLogin) {

        let loginData;

        try {

          loginData =
            JSON.parse(text);

        } catch {

          throw new Error(
            "Invalid login response from server."
          );
        }

        const jwt =
          loginData.token;

        if (
          !jwt ||
          typeof jwt !== "string"
        ) {

          throw new Error(
            "Login succeeded but no token was returned."
          );
        }

        const cleanJwt =
          jwt
            .replace(/^Bearer\s+/i, "")
            .trim();

        const parts =
          cleanJwt.split(".");

        if (
          parts.length !== 3
        ) {

          throw new Error(
            "Invalid JWT received from server."
          );
        }

        localStorage.setItem(
          "token",
          cleanJwt
        );

        setToken(
          cleanJwt
        );

        setEmail("");
        setPassword("");
        setMessage("");

      }

      // ======================================
      // REGISTER SUCCESS
      // ======================================

      else {

        setIsLogin(true);

        setName("");
        setPassword("");

        setMessage(
          "Account created successfully. Please login."
        );
      }

    } catch (error) {

      console.error(
        "Auth error:",
        error
      );

      setMessage(
        error.message ||
        "Unable to connect to ReviewForge."
      );

    } finally {

      setLoading(false);

    }
  };

  // ==========================================
  // LOGOUT
  // ==========================================

  const logout = () => {

    localStorage.removeItem(
      "token"
    );

    setToken(null);

    setResult(null);

    setRepoUrl("");

    setMessage("");

    setEmail("");

    setPassword("");
  };

  // ==========================================
  // REVIEW REPOSITORY
  // ==========================================

  const reviewRepository = async () => {

    setMessage("");

    setResult(null);

    if (!repoUrl.trim()) {

      setMessage(
        "Please enter a GitHub repository URL."
      );

      return;
    }

    if (!token) {

      setMessage(
        "Please login first."
      );

      return;
    }

    const cleanToken =
      token
        .replace(/^Bearer\s+/i, "")
        .replace(/\s+/g, "")
        .trim();

    if (
      cleanToken.split(".").length !== 3
    ) {

      setMessage(
        "Your login session is invalid. Please login again."
      );

      logout();

      return;
    }

    setLoading(true);

    try {

      console.log(
        "Sending review request to:",
        `${API_URL}/api/review`
      );

      const response =
        await fetch(
          `${API_URL}/api/review`,
          {
            method: "POST",

            headers: {
              "Content-Type":
                "application/json",

              "Authorization":
                `Bearer ${cleanToken}`,
            },

            body:
              JSON.stringify({
                repoUrl:
                  repoUrl.trim(),
              }),
          }
        );

      const text =
        await response.text();

      console.log(
        "Review response:",
        response.status
      );

      let data;

      try {

        data =
          JSON.parse(text);

      } catch {

        data = {
          message: text,
        };
      }

      // ======================================
      // HANDLE ERROR
      // ======================================

      if (!response.ok) {

        if (
          response.status === 401
        ) {

          localStorage.removeItem(
            "token"
          );

          setToken(null);

          throw new Error(
            "Your session expired. Please login again."
          );
        }

        throw new Error(

          data?.details ||

          data?.error ||

          data?.message ||

          `Review request failed. Status: ${response.status}`
        );
      }

      if (data?.error) {

        throw new Error(
          data.error
        );
      }

      // ======================================
      // SUCCESS
      // ======================================

      setResult(data);

    } catch (error) {

      console.error(
        "Review error:",
        error
      );

      setMessage(
        error.message ||
        "Unable to analyze repository."
      );

    } finally {

      setLoading(false);

    }
  };

  // ==========================================
  // SECURITY CODE PAGE
  // ==========================================

  if (!securityVerified) {

    return (

      <div className="auth-page">

        <div className="auth-background-grid" />

        <div className="auth-card">

          <div className="auth-brand">

            <div className="brand-icon">
              RF
            </div>

            <div>

              <h1>
                ReviewForge
              </h1>

              <p>
                AI Code Review Platform
              </p>

            </div>

          </div>

          <div className="auth-heading">

            <span className="eyebrow">
              SECURE ACCESS
            </span>

            <h2>
              Enter Security Code
            </h2>

            <p>
              A valid security code is required
              to access ReviewForge.
            </p>

          </div>

          <div className="auth-form">

            <div className="input-group">

              <label>
                Security Code
              </label>

              <input
                type="password"
                placeholder="Enter security code"
                value={securityCode}
                onChange={(e) =>
                  setSecurityCode(e.target.value)
                }
                onKeyDown={(e) => {

                  if (e.key === "Enter") {
                    verifySecurityCode();
                  }

                }}
              />

            </div>

            <button
              className="primary-button full-width"
              onClick={verifySecurityCode}
              disabled={securityLoading}
            >

              {
                securityLoading
                  ? "Verifying..."
                  : "Enter ReviewForge"
              }

            </button>

            {securityMessage && (

              <div className="auth-message">
                {securityMessage}
              </div>

            )}

          </div>

          <div className="auth-footer">

            <span>
              Protected access
            </span>

            <span>
              Security code required
            </span>

          </div>

        </div>

      </div>
    );
  }

  // ==========================================
  // AUTH PAGE
  // ==========================================

  if (!token) {

    return (

      <div className="auth-page">

        <div className="auth-background-grid" />

        <div className="auth-card">

          <div className="auth-brand">

            <div className="brand-icon">
              RF
            </div>

            <div>

              <h1>
                ReviewForge
              </h1>

              <p>
                AI Code Review Platform
              </p>

            </div>

          </div>

          <div className="auth-heading">

            <span className="eyebrow">
              DEVELOPER TOOL
            </span>

            <h2>

              {
                isLogin
                  ? "Welcome back."
                  : "Build better code."
              }

            </h2>

            <p>

              {
                isLogin
                  ? "Sign in to analyze GitHub repositories."
                  : "Create an account and start reviewing code."
              }

            </p>

          </div>

          <div className="auth-form">

            {!isLogin && (

              <div className="input-group">

                <label>
                  Name
                </label>

                <input
                  type="text"
                  placeholder="Your name"
                  value={name}
                  onChange={(e) =>
                    setName(e.target.value)
                  }
                />

              </div>

            )}

            <div className="input-group">

              <label>
                Email
              </label>

              <input
                type="email"
                placeholder="you@example.com"
                value={email}
                onChange={(e) =>
                  setEmail(e.target.value)
                }
              />

            </div>

            <div className="input-group">

              <label>
                Password
              </label>

              <input
                type="password"
                placeholder="••••••••"
                value={password}
                onChange={(e) =>
                  setPassword(e.target.value)
                }
                onKeyDown={(e) => {

                  if (e.key === "Enter") {
                    handleAuth();
                  }

                }}
              />

            </div>

            <button
              className="primary-button full-width"
              onClick={handleAuth}
              disabled={loading}
            >

              {
                loading
                  ? "Please wait..."
                  : isLogin
                    ? "Sign in"
                    : "Create account"
              }

            </button>

            {message && (

              <div className="auth-message">
                {message}
              </div>

            )}

            <button
              className="text-button"
              onClick={() => {

                setIsLogin(!isLogin);

                setMessage("");

                setPassword("");

              }}
            >

              {
                isLogin
                  ? "Don't have an account? Create one"
                  : "Already have an account? Sign in"
              }

            </button>

          </div>

          <div className="auth-footer">

            <span>
              Secure authentication
            </span>

            <span>
              JWT protected
            </span>

          </div>

        </div>

      </div>
    );
  }

  // ==========================================
  // DASHBOARD
  // ==========================================

  return (

    <div className="dashboard">

      <header className="topbar">

        <div className="topbar-left">

          <div className="brand-icon small">
            RF
          </div>

          <div className="brand-name">

            <strong>
              ReviewForge
            </strong>

            <span>
              Code Intelligence
            </span>

          </div>

        </div>

        <div className="topbar-right">

          <div className="connection-status">

            <span className="status-dot" />

            Backend connected

          </div>

          <button
            className="logout-button"
            onClick={logout}
          >

            Logout

          </button>

        </div>

      </header>

      <main className="dashboard-content">

        <section className="dashboard-hero">

          <div className="hero-copy">

            <span className="eyebrow">
              AI-POWERED CODE ANALYSIS
            </span>

            <h2>

              Ship cleaner code.

              <br />

              <span>
                Review smarter.
              </span>

            </h2>

            <p>

              Connect a public GitHub repository and
              let ReviewForge analyze your codebase
              for bugs, security risks and improvements.

            </p>

          </div>

        </section>

        <section className="review-panel">

          <div className="panel-header">

            <div>

              <span className="panel-label">
                REPOSITORY
              </span>

              <h3>
                Start a code review
              </h3>

            </div>

            <div className="github-badge">
              GitHub
            </div>

          </div>

          <div className="repo-input-wrapper">

            <div className="url-prefix">
              ↗
            </div>

            <input
              type="text"
              value={repoUrl}
              onChange={(e) =>
                setRepoUrl(e.target.value)
              }
              onKeyDown={(e) => {

                if (e.key === "Enter") {
                  reviewRepository();
                }

              }}
              placeholder="https://github.com/username/repository"
            />

            <button
              className="primary-button"
              onClick={reviewRepository}
              disabled={loading}
            >

              {
                loading
                  ? "Analyzing..."
                  : "Review Repository"
              }

              <span>
                →
              </span>

            </button>

          </div>

          <div className="input-hint">

            <span>
              ✓
            </span>

            Public GitHub repositories supported

          </div>

        </section>

        {message && (

          <div className="dashboard-message">

            <span>
              !
            </span>

            {message}

          </div>

        )}

        {!result && !loading && (

          <section className="feature-grid">

            <div className="feature-card">

              <div className="feature-icon">
                ◈
              </div>

              <h4>
                RAG Analysis
              </h4>

              <p>
                Relevant code is retrieved from your
                repository before analysis.
              </p>

            </div>

            <div className="feature-card">

              <div className="feature-icon">
                ◇
              </div>

              <h4>
                Security Checks
              </h4>

              <p>
                Identify common security issues and
                risky coding patterns.
              </p>

            </div>

            <div className="feature-card">

              <div className="feature-icon">
                ≋
              </div>

              <h4>
                AI Insights
              </h4>

              <p>
                Get practical recommendations for
                improving code quality.
              </p>

            </div>

          </section>

        )}

        {loading && (

          <section className="analysis-loading">

            <div className="loading-spinner" />

            <h3>
              Analyzing repository
            </h3>

            <p>
              Cloning repository, retrieving relevant
              code and generating insights...
            </p>

          </section>

        )}

        {result && !loading && (

          <section className="results">

            <div className="results-header">

              <div>

                <span className="panel-label">
                  ANALYSIS RESULT
                </span>

                <h3>
                  Repository review
                </h3>

                <p>

                  {
                    result.repo_name ||
                    result.repository ||
                    "Repository analysis completed"
                  }

                </p>

              </div>

              <div className="analysis-complete">

                <span />

                Complete

              </div>

            </div>

            <div className="stats-grid">

              <div className="stat-card">

                <span className="stat-label">
                  FILES ANALYZED
                </span>

                <strong>

                  {
                    Array.isArray(result.files)
                      ? result.files.length
                      : result.files_analyzed || 0
                  }

                </strong>

                <small>
                  Source files
                </small>

              </div>

              <div className="stat-card">

                <span className="stat-label">
                  CODE CHUNKS
                </span>

                <strong>
                  {result.chunk_count ?? 0}
                </strong>

                <small>
                  Indexed segments
                </small>

              </div>

              <div className="stat-card">

                <span className="stat-label">
                  RETRIEVED
                </span>

                <strong>
                  {result.retrieved_chunks ?? 0}
                </strong>

                <small>
                  Relevant chunks
                </small>

              </div>

              <div className="stat-card">

                <span className="stat-label">
                  ENGINE
                </span>

                <strong>
                  {result.review_engine || "RAG"}
                </strong>

                <small>
                  Review engine
                </small>

              </div>

            </div>

            <div className="review-result-card">

              <div className="review-result-header">

                <div>

                  <span className="panel-label">
                    REVIEW FINDINGS
                  </span>

                  <h3>
                    Code quality report
                  </h3>

                </div>

                <div className="review-engine">

                  {
                    result.ai_used
                      ? result.review_engine || "AI"
                      : "Fallback Analysis"
                  }

                </div>

              </div>

              <div className="review-content markdown-content">

                <ReactMarkdown>
                  {cleanReview(result.review)}
                </ReactMarkdown>

              </div>

            </div>

            {Array.isArray(result.files) &&
              result.files.length > 0 && (

                <div className="files-card">

                  <div className="files-header">

                    <div>

                      <span className="panel-label">
                        SOURCE FILES
                      </span>

                      <h3>
                        Repository contents
                      </h3>

                    </div>

                    <span className="file-count">
                      {result.files.length} files
                    </span>

                  </div>

                  <div className="file-list">

                    {result.files
                      .slice(0, 8)
                      .map((file, index) => (

                        <div
                          className="file-item"
                          key={index}
                        >

                          <span className="file-icon">
                            {getFileIcon(file)}
                          </span>

                          <span className="file-name">
                            {file}
                          </span>

                        </div>

                      ))}

                  </div>

                  {result.files.length > 8 && (

                    <div className="more-files">

                      + {result.files.length - 8}
                      {" "}more files

                    </div>

                  )}

                </div>

              )}

          </section>

        )}

      </main>

      <footer className="dashboard-footer">

        <span>
          ReviewForge
        </span>

        <span>
          AI-assisted developer tooling
        </span>

      </footer>

    </div>
  );
}

export default App;