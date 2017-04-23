package org.dynmap.blockscan.statehandlers;

import net.minecraft.block.Block;
import net.minecraft.block.state.BlockStateContainer;

/**
 * This interface is used to define 'state handlers' for Dynmap
 * These are tools for mapping map data to corresponding BlockState selectors for a given type of blocks
 * Ultimately, this is logically replicating the code within the Block classes themselves, so that Dynmap
 * can use them safely and with performance from threads outside the MC server's threads.
 * 
 * @author Mike Primm
 *
 */
public interface IStateHandlerFactory {
    /**
     * This method is used to examining the BlockStateContainer of a block to determine if the state mapper can handle the given block
     * @param block - Block object
     * @param bsc - BlockStateContainer object
     * @returns IStateHandler if the handler factory believes it can handle this block type, null otherwise
     */
    public IStateHandler canHandleBlockState(Block block, BlockStateContainer bsc);
}
