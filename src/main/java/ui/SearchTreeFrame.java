package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import org.apache.commons.collections15.Transformer;

import chimp.search.SearchTreeEdge;
import chimp.search.SearchTreeNode;
import edu.uci.ics.jung.algorithms.layout.RadialTreeLayout;
import edu.uci.ics.jung.algorithms.layout.TreeLayout;
import edu.uci.ics.jung.graph.DelegateForest;
import edu.uci.ics.jung.graph.Forest;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;

/**
 * Viewer for the search tree built by {@link chimp.search.ChimpSearchEngine}.
 *
 * <p>Re-hosted from the Meta-CSP Framework's
 * {@code org.metacsp.utility.UI.SearchTreeFrame} (by Federico Pecora) after
 * the storage-layer swap: the underlying forest now uses CHIMP-native
 * {@link SearchTreeNode}/{@link SearchTreeEdge} types. Supports toggling between
 * a hierarchical (default) and a radial tree layout.
 */
public class SearchTreeFrame extends JFrame {

	private static final long serialVersionUID = -7124219242305675892L;

	private final Forest<SearchTreeNode, SearchTreeEdge> graph;
	private final VisualizationViewer<SearchTreeNode, SearchTreeEdge> vv;
	private final TreeLayout<SearchTreeNode, SearchTreeEdge> treeLayout;
	private final RadialTreeLayout<SearchTreeNode, SearchTreeEdge> radialLayout;

	public SearchTreeFrame(DelegateForest<SearchTreeNode, SearchTreeEdge> graph) {

		this.graph = graph;

		treeLayout = new TreeLayout<SearchTreeNode, SearchTreeEdge>(graph);
		radialLayout = new RadialTreeLayout<SearchTreeNode, SearchTreeEdge>(graph);
		radialLayout.setSize(new Dimension(600, 600));

		vv = new VisualizationViewer<SearchTreeNode, SearchTreeEdge>(treeLayout, new Dimension(600, 600));

		vv.setBackground(Color.white);
		vv.getRenderContext().setEdgeShapeTransformer(new EdgeShape.Line<SearchTreeNode, SearchTreeEdge>());

		vv.getRenderContext().setVertexLabelTransformer(new Transformer<SearchTreeNode, String>() {
			@Override
			public String transform(SearchTreeNode arg0) {
				return arg0.toString();
			}
		});

		vv.setVertexToolTipTransformer(new Transformer<SearchTreeNode, String>() {
			@Override
			public String transform(SearchTreeNode arg0) {
				return arg0.toString();
			}
		});

		// draw edge labels
		Transformer<SearchTreeEdge, String> stringer = new Transformer<SearchTreeEdge, String>() {
			@Override
			public String transform(SearchTreeEdge e) {
				return e.label();
			}
		};

		// light-gray edges, as in the original SearchTreeFrame
		Transformer<SearchTreeEdge, Paint> edgePaint = new Transformer<SearchTreeEdge, Paint>() {
			@Override
			public Paint transform(SearchTreeEdge arg0) {
				return Color.lightGray;
			}
		};

		// terminal nodes stand out: green for success, red for failure
		Transformer<SearchTreeNode, Paint> vertexPaint = new Transformer<SearchTreeNode, Paint>() {
			@Override
			public Paint transform(SearchTreeNode v) {
				if ("SUCCESS".equals(v.label())) return Color.green;
				if ("FAILURE".equals(v.label())) return Color.red;
				return Color.white;
			}
		};

		vv.getRenderContext().setEdgeDrawPaintTransformer(edgePaint);
		vv.getRenderContext().setArrowFillPaintTransformer(edgePaint);
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

		// toggle between hierarchical and radial layout
		JToggleButton layoutToggle = new JToggleButton("Radial layout");
		layoutToggle.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					vv.setGraphLayout(radialLayout);
				} else {
					vv.setGraphLayout(treeLayout);
				}
			}
		});

		JPanel scaleGrid = new JPanel(new GridLayout(1, 0));
		scaleGrid.setBorder(BorderFactory.createTitledBorder("Zoom"));

		JPanel controls = new JPanel();
		scaleGrid.add(plus);
		scaleGrid.add(minus);
		controls.add(scaleGrid);
		controls.add(modeBox);
		controls.add(layoutToggle);

		content.add(controls, BorderLayout.SOUTH);
	}

	public static SearchTreeFrame draw(DelegateForest<SearchTreeNode, SearchTreeEdge> graph) {
		SearchTreeFrame stf = new SearchTreeFrame(graph);
		stf.setTitle(SearchTreeFrame.class.getSimpleName());
		stf.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		stf.pack();
		stf.setVisible(true);
		return stf;
	}

}
