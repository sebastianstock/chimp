package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.apache.commons.collections15.Transformer;
import org.metacsp.framework.Constraint;
import org.metacsp.framework.Variable;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import fluentSolver.Fluent;
import fluentSolver.FluentConstraint;

/**
 * Based on SearchTreeFrame form the MetaCSP-Framework by Federico Pecora.
 *
 */

public class PlanHierarchyFrame extends JFrame {

	private static final long serialVersionUID = -5124219242305675891L;


	Graph<Fluent,FluentConstraint> graph;

	VisualizationViewer<Fluent,FluentConstraint> vv;

	public PlanHierarchyFrame(Graph<Fluent,FluentConstraint> graph, int distx) {

		this.graph = graph;
		
		HierarchicalLayout hLayout = new HierarchicalLayout(graph, distx);
		vv =  new VisualizationViewer<Fluent, FluentConstraint>(hLayout, new Dimension(600,600));

		vv.setBackground(Color.white);
		vv.getRenderContext().setEdgeShapeTransformer(new EdgeShape.Line<Fluent,FluentConstraint>());
		vv.getRenderContext().setVertexLabelTransformer(new Transformer<Fluent, String>() {
			@Override
			public String transform(Fluent arg0) {
				return arg0.getName();
			}
		});

		vv.setVertexToolTipTransformer(new Transformer<Fluent, String>() {
			@Override
			public String transform(Fluent arg0) {
				return arg0.toString();
			}
		});

		vv.getRenderContext().setArrowFillPaintTransformer(new Transformer<FluentConstraint, Paint>() {
			@Override
			public Paint transform(FluentConstraint arg0) {
				return Color.lightGray;
			}
		});
		
		
		//draw edge labels
		Transformer<FluentConstraint,String> stringer = new Transformer<FluentConstraint,String>(){
			@Override
			public String transform(FluentConstraint e) {
				try { return e.getEdgeLabel(); }
				catch (NullPointerException ex) { return ""; }
			}
		};

		Transformer<Fluent,Paint> vertexPaint = new Transformer<Fluent,Paint>() {
			public Paint transform(Fluent v) {
				return v.getColor();
			}
		};
		
		vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);
		vv.getRenderContext().setEdgeLabelTransformer(stringer);

		Container content = getContentPane();
		final GraphZoomScrollPane panel = new GraphZoomScrollPane(vv);
		content.add(panel);

		final DefaultModalGraphMouse<?, ?> graphMouse = new DefaultModalGraphMouse<Object, Object>();

		vv.setGraphMouse(graphMouse);

		JComboBox modeBox = graphMouse.getModeComboBox();
		modeBox.addItemListener(graphMouse.getModeListener());
		graphMouse.setMode(Mode.TRANSFORMING);

		final ScalingControl scaler = new CrossoverScalingControl();

		JButton plus = new JButton("+");
		plus.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				scaler.scale(vv, 1.1f, vv.getCenter());
			}
		});
		JButton minus = new JButton("-");
		minus.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				scaler.scale(vv, 1/1.1f, vv.getCenter());
			}
		});

		JPanel scaleGrid = new JPanel(new GridLayout(1,0));
		scaleGrid.setBorder(BorderFactory.createTitledBorder("Zoom"));

		JPanel controls = new JPanel();
		scaleGrid.add(plus);
		scaleGrid.add(minus);
		controls.add(scaleGrid);
		controls.add(modeBox);

		content.add(controls, BorderLayout.SOUTH);
	}


	public static PlanHierarchyFrame draw(Graph<Fluent,FluentConstraint> graph, int distx) {
		PlanHierarchyFrame stf = new PlanHierarchyFrame(graph, distx);
		stf.setTitle(PlanHierarchyFrame.class.getSimpleName());
		stf.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		stf.pack();
		stf.setVisible(true);
		return stf;
	}

}
