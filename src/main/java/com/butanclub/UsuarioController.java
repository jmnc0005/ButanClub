/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.butanclub;

import com.butanclub.dao.UsuarioDAO;
import com.butanclub.jdbc.UsuarioDAOjdbc;
import com.butanclub.model.Usuario;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.butanclub.ConciertoController;
import com.butanclub.dao.ConciertoDAO;
import com.butanclub.jdbc.ConciertoDAOjdbc;
import com.butanclub.model.Concierto;
import javax.servlet.annotation.HttpConstraint;
import javax.servlet.annotation.ServletSecurity;

/**
 *
 * @author Pedro Luis
 */
@WebServlet(urlPatterns = {"/usuarios/*"})
@ServletSecurity(
        @HttpConstraint(rolesAllowed = {"Administrador", "Artista", "Registrado"}))

public class UsuarioController extends HttpServlet {

    private UsuarioDAO usuarios;
    private ConciertoDAO conciertos;
    String svlURL;
    final String srvViewPath = "/WEB-INF/usuarios";

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);

        svlURL = servletConfig.getServletContext().getContextPath() + "/usuarios";

        usuarios = new UsuarioDAOjdbc();
        conciertos = new ConciertoDAOjdbc();
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        response.setContentType("text/html");
        request.setAttribute("svlURL", svlURL);
        request.setCharacterEncoding("UTF-8");

        if (request.authenticate(response)) {

            Usuario usuario = usuarios.buscaUsuario(request.getRemoteUser());
            request.setAttribute("log", usuario);
        }

    }

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        processRequest(request, response);
        RequestDispatcher rd = null;
        String action = (request.getPathInfo() != null ? request.getPathInfo() : "");

        switch (action) {

            case "/edita": {
                String usuarioEdita = request.getParameter("usuario-edita");
                Usuario usu = usuarios.buscaUsuario(usuarioEdita);
                request.setAttribute("usuario", usu);
                rd = request.getRequestDispatcher(srvViewPath + "/EditaUsuario.jsp");
                break;
            }
            case "/borra": {
                String usuarioBorra = request.getParameter("usuario-borra");
                usuarios.borra(usuarioBorra);
                //response.sendRedirect(svlURL);
                //response.sendRedirect(srvViewPath + "/infoUsuario.jsp");
                List<Usuario> lu = usuarios.buscaTodos();
                request.setAttribute("listadoUsuarios", lu);
                rd = request.getRequestDispatcher(srvViewPath + "/infoUsuario.jsp");
                break;
            }
            case "/RegistroUsuario": {
                Usuario usu = new Usuario();
                request.setAttribute("usuario", usu);
                rd = request.getRequestDispatcher(srvViewPath + "/RegistroUsuario.jsp");
                break;
            }
            case "/borraconcierto": {

                response.sendRedirect("/ButanClub/conciertos");
                return;
            }

            case "/acceso": {
                //cargamos los conciertos
                List<Concierto> lc = conciertos.buscaTodos();

                request.getSession().setAttribute("listadoConciertos", lc);

                //cargamos los conciertos del usuario
                Usuario usuario = usuarios.buscaUsuario(request.getRemoteUser());
                String IDUsuario = usuario.getUsuario();
                List<Concierto> lcu = conciertos.buscaConciertosUsuario(IDUsuario);

                request.getSession().setAttribute("listadoConciertosUsuario", lcu);

                //cargamos los usuarios
                List<Usuario> lu = usuarios.buscaTodos();
                request.setAttribute("listadoUsuarios", lu);

                request.getSession().setAttribute("log", usuario);
                rd = request.getRequestDispatcher(srvViewPath + "/infoUsuarioAdministrador.jsp");
                break;
            }

            default:
                if (request.isUserInRole("Registrado")) {
                    rd = request.getRequestDispatcher(srvViewPath + "/infoUsuario.jsp");
                }

                if (request.isUserInRole("Artista")) {
                    rd = request.getRequestDispatcher(srvViewPath + "/infoUsuarioArtista.jsp");
                }

                if (request.isUserInRole("Administrador")) {
                    response.sendRedirect(svlURL + "/acceso");
                    return;
                }

                break;

        }

        rd.forward(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
        RequestDispatcher rd = request.getRequestDispatcher("");

        String action = (request.getPathInfo() != null ? request.getPathInfo() : "");
        switch (action) {

            case "/edita": {
                Usuario usu = new Usuario();
                if (validaUsuario(request, usu)) {
                    usu.setTipoUsuario(request.getParameter("tipoUsuario"));
                    usuarios.guarda(usu);
                    List<Usuario> lu = usuarios.buscaTodos();
                    request.setAttribute("listadoUsuarios", lu);
                    rd = request.getRequestDispatcher(srvViewPath + "/infoUsuario.jsp");
                    break;
                }
            }

            case "/RegistroUsuario": {
                Usuario usu = new Usuario();
                if (validaUsuario(request, usu)) {
                    usuarios.crea(usu);
                    rd = request.getRequestDispatcher(srvViewPath + "/NuevoUsuario.jsp");
                    break;
                }
            }
        }
        rd.forward(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    private boolean validaUsuario(HttpServletRequest request, Usuario usu) {
        usu.setApellidos(request.getParameter("apellidos"));
        usu.setContraseña(request.getParameter("pass"));
        usu.setCorreo(request.getParameter("email"));
        usu.setNombre(request.getParameter("nombre"));
        usu.setTelefono(request.getParameter("tlfn"));
        usu.setTipoUsuario("Registrado");
        usu.setUsuario(request.getParameter("usuario"));
        usu.setfNacimiento(request.getParameter("fecha"));

        return true;
    }
}
