package fr.inria.stamp;

import fr.inria.stamp.tavern.Item;
import fr.inria.stamp.tavern.Player;
import fr.inria.stamp.tavern.Seller;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 05/09/17
 */
public class MainTest {

	public int aUsedNumber = 3;

	private int getANumber() {
		return 0;
	}

	@Before
	public void setUp() throws Exception {

	}

	@After
	public void tearDown() throws Exception {

	}

	@Test
	public void test2() throws Exception {
		String b = null;
		assertNull(b);
	}

	@Test
	public void test() throws Exception {
		System.out.println(this.aUsedNumber);
		System.out.println(getANumber());
		System.out.println("\"bar\"");
		System.out.println("NEW\nLINE");
		System.out.println(true);
		System.out.println('<');
		System.out.println('\'');
		byte b = 3;
		short s = 3;
		int i = 3;
		long l = 3;

		Seller seller = new Seller(100, Collections.singletonList(new Item("Potion", 5)));
		Player player = new Player("Timoleon", 1000);

		assertEquals("Player{gold=1000, items=[]}", player.toString());
		assertEquals("Seller{gold=100, items=[Potion]}", seller.toString());

		player.buyItem("Potion", seller);

		assertEquals("Player{gold=995, items=[Potion]}", player.toString());
		assertEquals("Seller{gold=105, items=[Potion]}", seller.toString());
	}

	@Test
	public void test3() throws Exception {
		Seller seller = new Seller(100, Collections.singletonList(new Item("Potion", 5)));
		Player player = new Player("Timoleon", 1000);
		assertEquals("Player{gold=1000, items=[]}", player.toString());
		assertEquals("Seller{gold=100, items=[Potion]}", seller.toString());
		player.buyItem("Potion", seller);
		assertEquals("Player{gold=995, items=[Potion]}", player.toString());
		assertEquals("Seller{gold=105, items=[Potion]}", seller.toString());
	}

}
