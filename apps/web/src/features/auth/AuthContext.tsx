import { createContext, ReactNode, useContext, useMemo, useState } from "react";

type AuthContextType = {
  token: string;
  userId: string;
  setToken: (token: string) => void;
  clearToken: () => void;
};

const AuthContext = createContext<AuthContextType | undefined>(undefined);
const TOKEN_KEY = "dmsa.web.token";

function readInitialToken(): string {
  const token = localStorage.getItem(TOKEN_KEY);
  return token ?? "";
}

function readUserId(token: string): string {
  try {
    const payload = token.split(".")[1];
    if (!payload) {
      return "";
    }
    const decoded = JSON.parse(atob(payload.replace(/-/g, "+").replace(/_/g, "/"))) as { sub?: string };
    return decoded.sub ?? "";
  } catch {
    return "";
  }
}

type AuthProviderProps = {
  children: ReactNode;
};

export function AuthProvider({ children }: AuthProviderProps) {
  const [token, setTokenState] = useState<string>(readInitialToken);

  const value = useMemo<AuthContextType>(
    () => ({
      token,
      userId: readUserId(token),
      setToken: (next) => {
        setTokenState(next);
        localStorage.setItem(TOKEN_KEY, next);
      },
      clearToken: () => {
        setTokenState("");
        localStorage.removeItem(TOKEN_KEY);
      }
    }),
    [token]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used inside AuthProvider");
  }
  return context;
}
