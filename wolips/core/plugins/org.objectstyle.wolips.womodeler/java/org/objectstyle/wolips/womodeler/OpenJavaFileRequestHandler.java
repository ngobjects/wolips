package org.objectstyle.wolips.womodeler;

import java.util.Map;
import java.util.Objects;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.objectstyle.wolips.womodeler.server.IRequestHandler;
import org.objectstyle.wolips.womodeler.server.Request;
import org.objectstyle.wolips.womodeler.server.Webserver;

public class OpenJavaFileRequestHandler implements IRequestHandler {

	public void init( Webserver server ) throws Exception {
		// DO NOTHING
	}

	public void handle( Request request ) throws Exception {
		final Map<String, String> params = request.getQueryParameters();
		final String appName = params.get( "app" );
		final String className = params.get( "className" );
		final String lineNumber = params.get( "lineNumber" );

		Objects.requireNonNull( appName );
		Objects.requireNonNull( className );
		Objects.requireNonNull( lineNumber );

		final IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject( appName );

		if( project != null ) {
			if( className != null ) {
				Display.getDefault().asyncExec( new Runnable() {
					public void run() {
						IJavaProject javaProject = JavaCore.create( project );
						try {
							IType type = javaProject.findType( className );
							if( type != null ) {
								IEditorPart editorPart = JavaUI.openInEditor( type, true, true );

								if( editorPart instanceof ITextEditor ) {
									ITextEditor editor = (ITextEditor)editorPart;

									IDocumentProvider provider = editor.getDocumentProvider();
									IDocument document = provider.getDocument( editor.getEditorInput() );

									try {
										int lineStart = document.getLineOffset( Integer.parseInt( lineNumber ) );
										editor.selectAndReveal( lineStart, 0 );

										//    					     page.activate(editor);
									}
									catch( BadLocationException x ) {
										// ignore
									}
								}

							}
						}
						catch( Throwable e1 ) {
							e1.printStackTrace();
						}
					}
				} );
			}
		}
		request.getWriter().println( "ok" );
	}
}