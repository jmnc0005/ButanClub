/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.butanclub;

import com.butanclub.dao.ConciertoDAO;
import com.butanclub.dao.EntradaDAO;
import com.butanclub.jdbc.ConciertoDAOjdbc;
import com.butanclub.jdbc.EntradaDAOjdbc;
import com.butanclub.model.Concierto;
import com.butanclub.model.Entrada;
import com.butanclub.model.Usuario;
import java.io.IOException;
import java.util.List;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Pedro Luis
 */
@WebServlet(name = "controlConciertos", urlPatterns = {"/conciertos/*"})
public class ConciertoController extends HttpServlet {

    private EntradaDAO entradas;
    private ConciertoDAO conciertos;
    String svlURL;
    final String srvViewPath = "/WEB-INF/conciertos";

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

        svlURL = servletConfig.getServletContext().getContextPath() + "/conciertos";

        conciertos = (ConciertoDAO) new ConciertoDAOjdbc();
        entradas = new EntradaDAOjdbc();

        List<Concierto> list = conciertos.buscaTodos();
        getServletContext().setAttribute("listadoConciertos", list);

    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        response.setContentType("text/html");
        request.setAttribute("svlURL", svlURL);
        request.setCharacterEncoding("UTF-8");

    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
        RequestDispatcher rd = null;
        String action = (request.getPathInfo() != null ? request.getPathInfo() : "");
        switch (action) {

            case "/solicitarSala": {
                Concierto c = new Concierto();
                if (validaConcierto(request, c)) {
                    conciertos.crea(c);
                    response.sendRedirect("/ButanClub/usuarios/respuestaConciertosUsuario");
                    return;
                }

                break;
            }
            case "/proximos": {

                List<Concierto> lc = conciertos.buscaTodos();
                request.setAttribute("listadoConciertos", lc);
                rd = request.getRequestDispatcher(srvViewPath + "/conciertos.jsp");
                break;
            }
            default:
                List<Concierto> lista = conciertos.buscaProximosConciertos();
                getServletContext().setAttribute("listadoProximosConciertos", lista);
                rd = request.getRequestDispatcher(srvViewPath + "/inicio.jsp");
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
        RequestDispatcher rd = null;
        String action = (request.getPathInfo() != null ? request.getPathInfo() : "");
        switch (action) {
            case "/comprarEntrada": {
                int idconcierto = Integer.parseInt(request.getParameter("idconcierto"));
                Concierto c = conciertos.buscaConcierto(idconcierto);
                request.setAttribute("conciertoCompra", c);
                rd = request.getRequestDispatcher(srvViewPath + "/comprar-entrada.jsp");
                break;
            }
            case "/ConfirmacionCompra": {
                String usuario = request.getParameter("usuario-comprador");
                int idConcierto = Integer.parseInt(request.getParameter("concierto-comprado"));
                int cantidad = Integer.parseInt(request.getParameter("numero-entradas"));

                Concierto c = conciertos.buscaConcierto(idConcierto);
                request.setAttribute("conciertoCompra", c);

                Entrada entrada = new Entrada(usuario, idConcierto, cantidad);
                entradas.crea(entrada);
                request.setAttribute("entrada", entrada);
                rd = request.getRequestDispatcher(srvViewPath + "/ConfirmacionCompra.jsp");
                break;

            }

        }
        rd.forward(request, response);
    }

    private boolean validaConcierto(HttpServletRequest request, Concierto c) {
        c.setNombre(request.getParameter("nombre"));
        c.setArtista(request.getParameter("artista"));
        c.setFecha(request.getParameter("fecha"));
        c.setGenero(request.getParameter("genero"));
        c.setHora(request.getParameter("hora"));

        c.setPrecio(Integer.parseInt(request.getParameter("precio")));

        return true;
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

}
